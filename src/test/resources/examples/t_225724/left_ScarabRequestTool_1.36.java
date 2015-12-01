package org.tigris.scarab.tools;

/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
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

// Turbine
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.ComboKey;
import org.apache.turbine.RunData;
import org.apache.turbine.modules.Module;
import org.apache.turbine.tool.IntakeTool;
import org.apache.turbine.services.intake.model.Group;
import org.apache.turbine.util.Log;
import org.apache.fulcrum.pool.Recyclable;

// Scarab
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.QueryPeer;
import org.tigris.scarab.om.Depend;
import org.tigris.scarab.om.DependPeer;
import org.tigris.scarab.om.ScarabModulePeer;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentPeer;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionPeer;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.services.module.ModuleManager;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.word.IssueSearch;

/**
 * This class is used by the Scarab API
 */
public class ScarabRequestTool implements ScarabRequestScope,
                                          Recyclable
{
    /** the object containing request specific data */
    private RunData data;

    /**
     * A User object for use within the Scarab API.
     */
    private ScarabUser user = null;

    /**
     * A Issue object for use within the Scarab API.
     */
    private Issue issue = null;

    /**
     * A Attribute object for use within the Scarab API.
     */
    private Attribute attribute = null;

    /**
     * A Attachment object for use within the Scarab API.
     */
    private Attachment attachment = null;

    /**
     * A Depend object for use within the Scarab API.
     */
    private Depend depend = null;

    /**
     * A Query object for use within the Scarab API.
     */
    private Query query = null;

    /**
     * A ModuleEntity object for use within the Scarab API.
     */
    private ModuleEntity module = null;

    /**
     * A AttributeOption object for use within the Scarab API.
     */
    private AttributeOption attributeOption = null;
    
    public void init(Object data)
    {
        this.data = (RunData)data;
    }

    /**
     * nulls out the issue and user objects
     */
    public void refresh()
    {
        this.user = null;
        this.issue = null;
    }

    /**
     * Constructor does initialization stuff
     */    
    public ScarabRequestTool()
    {
        //intake = new IntakeSystem();
    }

    /**
     * A Attribute object for use within the Scarab API.
     */
    public void setAttribute (Attribute attribute)
    {
        this.attribute = attribute;
    }

    /**
     * A Depend object for use within the Scarab API.
     */
    public void setDepend (Depend depend)
    {
        this.depend = depend;
    }

    /**
     * A Query object for use within the Scarab API.
     */
    public void setQuery (Query query)
    {
        this.query = query;
    }

    private IntakeTool getIntakeTool()
    {
        return (IntakeTool)Module.getTemplateContext(data)
            .get(ScarabConstants.INTAKE_TOOL);
    }


    /**
     * A Attribute object for use within the Scarab API.
     */
    public void setAttributeOption (AttributeOption option)
    {
        this.attributeOption = option;
    }

    /**
     * A Attribute object for use within the Scarab API.
     */
    public AttributeOption getAttributeOption()
        throws Exception
    {
try{
        if (attributeOption == null)
        {
            String optId = data.getParameters()
                .getString("currentAttributeOption"); 
            if ( optId == null )
            {
                attributeOption = new AttributeOption();                
            }
            else 
            {
                attributeOption = AttributeOptionPeer
                    .retrieveByPK(new NumberKey(optId));
            }
        }
}catch(Exception e){e.printStackTrace();}
        return attributeOption;
    }

    /**
     * A User object for use within the Scarab API.
     */
    public void setUser (ScarabUser user)
    {
        // this.user = user;
    }

    /**
     * A User object for use within the Scarab API.
     */
    public ScarabUser getUser()
    {
        if (user == null)
        {
            this.user = (ScarabUser)data.getUser();
        }
        return (ScarabUser)data.getUser();
    }

    /**
     * A User object for use within the Scarab API.
     */
    public ScarabUser getUser(String id)
     throws Exception
    {
        ScarabUser su = null;
        try
        {
            ObjectKey pk = (ObjectKey)new NumberKey(id);
            su = (ScarabUser)ScarabUserImplPeer.retrieveScarabUserImplByPK(pk);
        }
        catch (Exception e)
        {
            Log.error ("SRT.getUser(id) error: ", e);
        }
        return su;
    }

    /**
     * A User object for use within the Scarab API.
     */
    public ScarabUser getUser(Integer id)
        throws Exception
    {
        return getUser(id.toString());
    }

    /**
     * A Attribute object for use within the Scarab API.
     */
    public Attribute getAttribute()
     throws Exception
    {
try{
        if (attribute == null)
        {
            String attId = getIntakeTool()
                .get("Attribute", IntakeTool.DEFAULT_KEY).get("Id").toString();
            if ( attId == null || attId.length() == 0 )
            {
                attribute = Attribute.getInstance();
            }
            else 
            {
                attribute = Attribute.getInstance(new NumberKey(attId));
            }
        }        
}catch(Exception e){e.printStackTrace();}
        return attribute;
 
   }

    /**
     * A Query object for use within the Scarab API.
     */
    public Query getQuery()
     throws Exception
    {
        try
        {
            if (query == null)
            {
                String queryId = getIntakeTool()
                    .get("Query", IntakeTool.DEFAULT_KEY).get("Id").toString();
                if ( queryId == null || queryId.length() == 0 )
                {
                    query = Query.getInstance();
                }
                else 
                {
                    query = QueryPeer.retrieveByPK(new NumberKey(queryId));
                }
            }        
        }        
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return query;
 
   }

    /**
     * A Depend object for use within the Scarab API.
     */
    public Depend getDepend()
     throws Exception
    {
        try
        {
            if (depend == null)
            {
                String dependId = getIntakeTool()
                    .get("Depend", IntakeTool.DEFAULT_KEY).get("Id").toString();
                if ( dependId == null || dependId.length() == 0 )
                {
                    depend = Depend.getInstance();
                }
                else 
                {
                    depend = DependPeer.retrieveByPK(new NumberKey(dependId));
                }
            }        
        }        
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return depend;
 
   }

    /**
     * A Attachment object for use within the Scarab API.
     */
    public Attachment getAttachment()
     throws Exception
    {
try{
        if (attachment == null)
        {
            Group att = getIntakeTool()
                .get("Attachment", IntakeTool.DEFAULT_KEY, false);
            if ( att != null ) 
            {            
                String attId =  att.get("Id").toString();
                if ( attId == null || attId.length() == 0 )
                {
                    attachment = new Attachment();
                }
                else 
                {
                    attachment = AttachmentPeer
                        .retrieveByPK(new NumberKey(attId));
                }
            }
            else 
            {
                attachment = new Attachment();
            }
        }        
}catch(Exception e){e.printStackTrace(); throw e;}
        return attachment;
 
   }

    /**
     * A Module object for use within the Scarab API.
     */
    public void setModule(ModuleEntity module)
    {
        this.module = module;
    }

    /**
     * Get an Module object. 
     *
     * @return a <code>ModuleEntity</code> value
     */
    public ModuleEntity getModule()
     throws Exception
    {
      try{
            String modId = getIntakeTool()
                .get("Module", IntakeTool.DEFAULT_KEY).get("Id").toString();
            if ( modId == null || modId.length() == 0 )
            {
                module = ModuleManager.getInstance();
            }
            else 
            {
                module = ModuleManager.getInstance(new NumberKey(modId));
            }
      }catch(Exception e){e.printStackTrace();}
        return module;
 
   }
    /**
     * Get an RModuleAttribute object. 
     *
     * @return a <code>Module</code> value
     */
    public RModuleAttribute getRModuleAttribute()
        throws Exception
    {
        RModuleAttribute rma = null;
      try{
            ComboKey rModAttId = (ComboKey)getIntakeTool()
                .get("RModuleAttribute", IntakeTool.DEFAULT_KEY)
                .get("Id").getValue();
            if ( rModAttId == null )
            {
                NumberKey attId = (NumberKey)getIntakeTool()
                    .get("Attribute", IntakeTool.DEFAULT_KEY)
                    .get("Id").getValue();
                if ( attId != null && getUser().getCurrentModule() != null )
                {
                    NumberKey[] nka = {attId, 
                        getUser().getCurrentModule().getModuleId()};
                    rma = RModuleAttributePeer.retrieveByPK(new ComboKey(nka));
                }
                else 
                {
                    rma = new RModuleAttribute();
                }
            }
            else 
            {
                rma = RModuleAttributePeer.retrieveByPK(rModAttId);
            }
      }catch(Exception e){e.printStackTrace();}
        return rma;
 
   }

    

    /**
     * Get a specific module by key value.
     *
     * @param key a <code>String</code> value
     * @return a <code>Module</code> value
     */
    public ModuleEntity getModule(String key) throws Exception
    {
        return (ModuleEntity) ScarabModulePeer.retrieveByPK(new NumberKey(key));
    }


    /**
     * A Issue object for use within the Scarab API.
     */
    public void setIssue(Issue issue)
    {
        this.issue = issue;
    }

    /**
     * Get an Issue object. If it is the first time calling,
     * it will be a new blank issue object.
     *
     * @return a <code>Issue</code> value
     */
    public Issue getIssue()
        throws Exception
    {
        if (issue == null)
        {
            Group issueGroup = getIntakeTool()
                .get("Issue", IntakeTool.DEFAULT_KEY, false);
            if ( issueGroup != null ) 
            {            
                String issueId =  issueGroup.get("Id").toString();
                if ( issueId == null || issueId.length() == 0 )
                {
                    issue = new Issue();
                }
                else 
                {
                    issue = IssuePeer
                        .retrieveByPK(new NumberKey(issueId));
                }
            }
            else if ( data.getParameters().getString("issue_id") != null ) 
            {                
                String issueId = data.getParameters().getString("issue_id");
                if ( issueId.length() == 0 )
                {
                    issue = new Issue();
                }
                else 
                {
                    issue = IssuePeer
                        .retrieveByPK(new NumberKey(issueId));
                }
            }
            else 
            {
                issue = new Issue();
            }
        }        

        return issue;
    }

    /**
     * Get a new SearchIssue object. 
     *
     * @return a <code>Issue</code> value
     */
    public IssueSearch getSearch()
        throws Exception
    {
        IssueSearch search = new IssueSearch();
        search.setModuleCast(((ScarabUser)getUser()).getCurrentModule());
        return search;
    }

    /**
     * The id may be a primary key or an issue id.
     *
     * @param key a <code>String</code> value
     * @return a <code>Issue</code> value
     */
    public Issue getIssue(String key)
    {
        Issue issue = null;
        try
        {
            issue = IssuePeer.retrieveByPK(new NumberKey(key));
        }
        catch (Exception e)
        {
            // was not a primary key, try fid
            Issue.FederatedId fid = new Issue.FederatedId(key);
            if ( fid.getDomain() == null ) 
            {
                // handle null (always null right now)
            }
            issue = Issue.getIssueById(fid);
        }
        return issue;
    }



    // ****************** Recyclable implementation ************************

    private boolean disposed;

    /**
     * Recycles the object for a new client. Recycle methods with
     * parameters must be added to implementing object and they will be
     * automatically called by pool implementations when the object is
     * taken from the pool for a new client. The parameters must
     * correspond to the parameters of the constructors of the object.
     * For new objects, constructors can call their corresponding recycle
     * methods whenever applicable.
     * The recycle methods must call their super.
     */
    public void recycle()
    {
        disposed = false;
    }

    /**
     * Disposes the object after use. The method is called
     * when the object is returned to its pool.
     * The dispose method must call its super.
     */
    public void dispose()
    {
        data = null;
        user = null;
        issue = null;
        attribute = null;

        disposed = true;
    }

    /**
     * Checks whether the recyclable has been disposed.
     * @return true, if the recyclable is disposed.
     */
    public boolean isDisposed()
    {
        return disposed;
    }
}



