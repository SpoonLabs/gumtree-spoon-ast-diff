package org.tigris.scarab.actions;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 *
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 *
 * 5. Products derived from this software may not use the "Tigris" or
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without
 * prior written permission of Collab.Net.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */

import java.util.Calendar;

// Turbine Stuff
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.modules.ContextAdapter;

import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.security.util.TurbineSecurityException;
import org.apache.fulcrum.template.TemplateEmail;

import org.apache.commons.util.GenerateUniqueId;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.actions.base.ScarabTemplateAction;

/**
 * This class is responsible for dealing with the Forgot Password
 * Action.
 *
 * @author <a href="mailto:kevin.minshull@bitonic.com">Kevin Minshull</a>
 */
public class ForgotPassword extends ScarabTemplateAction
{
    /**
     The maximum length for the unique identifier used for the password.
     */
    private static final int UNIQUE_ID_MAX_LEN = 10;
    
    /**
     * This manages clicking the Forgot Password button
     */
    public void doForgotpassword(RunData data, TemplateContext context)
        throws Exception
    {
        data.setACL(null);
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid() && forgotPassword(data, context))
        {
            getScarabRequestTool(context).setConfirmMessage(
                "An email has been sent to you with your password.");
            setTarget(data, "Login.vm");
        }
    }
    
    /**
     * This takes care of looking the user up, setting the password to an arbitrary
     * value and sending the user an email
     */
    public boolean forgotPassword(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        
        Group password = intake.get("ForgotPassword", IntakeTool.DEFAULT_KEY);
        String username = password.get("Username").toString();
        
        ScarabUser user = null;
        try
        {
            user = (ScarabUser) TurbineSecurity.getUser(username);
            
            String tempPassword = GenerateUniqueId.getIdentifier();
            if (tempPassword.length() > UNIQUE_ID_MAX_LEN)
            {
                tempPassword = tempPassword.substring(0, UNIQUE_ID_MAX_LEN);
            }
            
            // first we need to save the user out of the session
            user.setPasswordExpire(Calendar.getInstance());
            user.setHasLoggedIn(Boolean.FALSE);
            data.setUser(user);
            data.save();

            // set the password to a temporary value then set the password to
            // expire now, forcing the user to change their password after login.
            TurbineSecurity.forcePassword(user, tempPassword);

            // place the password
            // in the context for use in the email template.
            context.put("password", tempPassword);

            TemplateEmail te = new TemplateEmail();
            te.setContext(new ContextAdapter(context));
            te.setTo(user.getFirstName() + " " + user.getLastName(), user.getEmail());
            te.setFrom(
                Turbine.getConfiguration()
                    .getString("scarab.email.forgotpassword.fromName",
                               "Scarab System"),
                Turbine.getConfiguration()
                    .getString("scarab.email.forgotpassword.fromAddress",
                               "help@localhost"));
            te.setSubject(
                Turbine.getConfiguration()
                    .getString("scarab.email.forgotpassword.subject",
                               "Account Password"));
            te.setTemplate(
                Turbine.getConfiguration()
                    .getString("scarab.email.forgotpassword.template",
                               "ForgotPassword.vm"));
            te.send();
        }
        catch (TurbineSecurityException e)
        {
            getScarabRequestTool(context).setAlertMessage("Invalid username.");
            Log.get().error("ForgotPassword: ", e);
            setTarget(data, "ForgotPassword.vm");
            return false;
        }
        return true;
    }    
}
