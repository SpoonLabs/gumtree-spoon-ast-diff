package org.tigris.scarab.om;

import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.util.ScarabConstants;

import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.workflow.WorkflowFactory;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class RModuleIssueType 
    extends org.tigris.scarab.om.BaseRModuleIssueType
    implements Persistent
{

    /**
     * Throws UnsupportedOperationException.  Use
     * <code>getModule()</code> instead.
     *
     * @return a <code>ScarabModule</code> value
     */
    public ScarabModule getScarabModule()
    {
        throw new UnsupportedOperationException(
            "Should use getModule");
    }

    /**
     * Throws UnsupportedOperationException.  Use
     * <code>setModule(Module)</code> instead.
     *
     */
    public void setScarabModule(ScarabModule module)
    {
        throw new UnsupportedOperationException(
            "Should use setModule(Module). Note module cannot be new.");
    }

    /**
     * Use this instead of setScarabModule.  Note: module cannot be new.
     */
    public void setModule(Module me)
        throws TorqueException
    {
        NumberKey id = me.getModuleId();
        if (id == null) 
        {
            throw new TorqueException("Modules must be saved prior to " +
                                      "being associated with other objects.");
        }
        setModuleId(id);
    }

    /**
     * Module getter.  Use this method instead of getScarabModule().
     *
     * @return a <code>Module</code> value
     */
    public Module getModule()
        throws TorqueException
    {
        Module module = null;
        ObjectKey id = getModuleId();
        if ( id != null ) 
        {
            module = ModuleManager.getInstance(id);
        }
        
        return module;
    }


    /**
     * Checks if user has permission to delete module-issue type mapping.
     */
    public void delete( ScarabUser user )
         throws Exception
    {                
        Module module = getModule();
        IssueType issueType = getIssueType();

        if (user.hasPermission(ScarabSecurity.MODULE__CONFIGURE, module))
        {
            // Delete attribute groups first
            List attGroups = module.getAttributeGroups(issueType);
            for (int j=0; j<attGroups.size(); j++)
            {
                // delete attribute-attribute group map
                AttributeGroup attGroup = 
                              (AttributeGroup)attGroups.get(j);
                attGroup.delete(user, module);
            }

            // Delete mappings with user attributes
            List rmas = module.getRModuleAttributes(issueType);
            for (int i=0; i<rmas.size(); i++)
            {
                ((RModuleAttribute)rmas.get(i)).delete(user);
            }
            // Delete mappings with user attributes for template type
            IssueType templateType = issueType.getTemplateIssueType();
            rmas = module.getRModuleAttributes(templateType);
            for (int i=0; i<rmas.size(); i++)
            {
                ((RModuleAttribute)rmas.get(i)).delete(user);
            }
 
            // delete workflows
            WorkflowFactory.getInstance().resetAllWorkflowsForIssueType(module, 
                                                                        issueType);

            Criteria c = new Criteria()
                .add(RModuleIssueTypePeer.MODULE_ID, getModuleId())
                .add(RModuleIssueTypePeer.ISSUE_TYPE_ID, getIssueTypeId());
            RModuleIssueTypePeer.doDelete(c);
            RModuleIssueTypeManager.removeFromCache(this);
            List rmits = module.getRModuleIssueTypes();
            rmits.remove(this);
        }
        else
        {
            throw new ScarabException(ScarabConstants.NO_PERMISSION_MESSAGE);
        }
    }

    /**
     * Not really sure why getDisplayText was created because 
     * it really should just be getDisplayName() (JSS)
     *
     * @see #getDisplayText()
     */
    public String getDisplayName()
    {
        String display = super.getDisplayName();
        if (display == null)
        {
            try
            {
                display = getIssueType().getName();
            }
            catch (TorqueException e)
            {
                log().error("Error getting the issue type name: ", e);
            }
        }
        return display;
    }

    /**
     * Gets name to display. First tries to get the DisplayName 
     * for the RMIT, if that is null, then it will get the IssueType's
     * name and use that.
     *
     * @deprecated use getDisplayName() instead
     */
    public String getDisplayText()
    {
        return this.getDisplayName();
    }

    public String getDisplayDescription()
    {
        String display = super.getDisplayDescription();
        if (display == null)
        {
            try
            {
                display = getIssueType().getDescription();
            }
            catch (TorqueException e)
            {
                log().error("Error getting the issue type description: ", e);
            }
        }
        return display;
    }

    /**
     * Copies object.
     */
    public RModuleIssueType copy()
         throws TorqueException
    {
        RModuleIssueType rmit2 = new RModuleIssueType();
        rmit2.setModuleId(getModuleId());
        rmit2.setIssueTypeId(getIssueTypeId());
        rmit2.setActive(getActive());
        rmit2.setDisplay(getDisplay());
        rmit2.setOrder(getOrder());
        rmit2.setDedupe(getDedupe());
        rmit2.setHistory(getHistory());
        rmit2.setComments(getComments());
        return rmit2;
    }

}
