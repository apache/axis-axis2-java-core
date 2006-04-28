package org.apache.axis2.tool.codegen.eclipse;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.tool.codegen.WSDL2JavaGenerator;
import org.apache.axis2.tool.codegen.eclipse.plugin.CodegenWizardPlugin;
import org.apache.axis2.tool.codegen.eclipse.ui.AbstractWizardPage;
import org.apache.axis2.tool.codegen.eclipse.ui.JavaSourceSelectionPage;
import org.apache.axis2.tool.codegen.eclipse.ui.JavaWSDLOptionsPage;
import org.apache.axis2.tool.codegen.eclipse.ui.JavaWSDLOutputLocationPage;
import org.apache.axis2.tool.codegen.eclipse.ui.OptionsPage;
import org.apache.axis2.tool.codegen.eclipse.ui.OutputPage;
import org.apache.axis2.tool.codegen.eclipse.ui.ToolSelectionPage;
import org.apache.axis2.tool.codegen.eclipse.ui.WSDLFileSelectionPage;
import org.apache.axis2.tool.codegen.eclipse.util.SettingsConstants;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.util.CommandLineOptionConstants;
import org.apache.ws.java2wsdl.Java2WSDLCodegenEngine;
import org.apache.ws.java2wsdl.utils.Java2WSDLCommandLineOption;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * The main wizard for the codegen wizard
 */

public class CodeGenWizard extends Wizard implements INewWizard,CommandLineOptionConstants.Java2WSDLConstants {
    private ToolSelectionPage toolSelectionPage;

    private WSDLFileSelectionPage wsdlSelectionPage;

    private OptionsPage optionsPage;

    private OutputPage outputPage;

    private JavaWSDLOptionsPage java2wsdlOptionsPage;

    private JavaSourceSelectionPage javaSourceSelectionPage;

    private JavaWSDLOutputLocationPage java2wsdlOutputLocationPage;

    private int selectedWizardType = SettingsConstants.WSDL_2_JAVA_TYPE;//TODO
                                                                        // change
                                                                        // this

    

   

    /**
     * Constructor for CodeGenWizard.
     */
    public CodeGenWizard() {
        super();
        setNeedsProgressMonitor(true);
        this
                .setWindowTitle(org.apache.axis2.tool.codegen.eclipse.plugin.CodegenWizardPlugin
                        .getResourceString("general.name"));
    }

    /**
     * Adding the page to the wizard.
     */

    public void addPages() {
        toolSelectionPage = new ToolSelectionPage();
        addPage(toolSelectionPage);

        //add the wsdl2java wizard pages
        wsdlSelectionPage = new WSDLFileSelectionPage();
        addPage(wsdlSelectionPage);
        optionsPage = new OptionsPage();
        addPage(optionsPage);
        outputPage = new OutputPage();
        addPage(outputPage);

        //add java2wsdl wizard pages
        javaSourceSelectionPage = new JavaSourceSelectionPage();
        addPage(javaSourceSelectionPage);
        java2wsdlOptionsPage = new JavaWSDLOptionsPage();
        addPage(java2wsdlOptionsPage);
        java2wsdlOutputLocationPage = new JavaWSDLOutputLocationPage();
        addPage(java2wsdlOutputLocationPage);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.IWizard#canFinish()
     */
    public boolean canFinish() {
        IWizardPage[] pages = getPages();
        AbstractWizardPage wizardPage = null;
        for (int i = 0; i < pages.length; i++) {
            wizardPage = (AbstractWizardPage) pages[i];
            if (wizardPage.getPageType() == this.selectedWizardType) {
                if (!(wizardPage.isPageComplete()))
                    return false;
            }
        }
        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        AbstractWizardPage currentPage = (AbstractWizardPage) page;
        AbstractWizardPage pageout = (AbstractWizardPage) super
                .getNextPage(page);

        while (pageout != null && selectedWizardType != pageout.getPageType()) {
            AbstractWizardPage temp = pageout;
            pageout = (AbstractWizardPage) super.getNextPage(currentPage);
            currentPage = temp;

        }
        return pageout;
    }

    /**
     * This method is called when 'Finish' button is pressed in the wizard. We
     * will create an operation and run it using wizard as execution context.
     */
    public boolean performFinish() {
        try {
            switch (selectedWizardType) {
            case SettingsConstants.WSDL_2_JAVA_TYPE:
                doFinishWSDL2Java();
                break;
            case SettingsConstants.JAVA_2_WSDL_TYPE:
                doFinishJava2WSDL();
                break;
            case SettingsConstants.UNSPECIFIED_TYPE:
                break; //Do nothing
            default:
                throw new RuntimeException(CodegenWizardPlugin.
                		getResourceString("general.invalid.state"));
            }
        } catch (Exception e) {
            MessageDialog.openError(getShell(), 
                    CodegenWizardPlugin.getResourceString("general.Error"), 
                    CodegenWizardPlugin.getResourceString("general.Error.prefix") +
                    e.getMessage());
            return false;
        }
        MessageDialog.openInformation(this.getShell(), 
                 CodegenWizardPlugin
                .getResourceString("general.name"), CodegenWizardPlugin
                .getResourceString("wizard.success"));
        return true;
    }

    /**
     * The worker method, generates the code itself.
     */
    private void doFinishWSDL2Java() {
        WorkspaceModifyOperation op = new WorkspaceModifyOperation()
        {
           protected void execute(IProgressMonitor monitor)
           throws CoreException, InvocationTargetException, InterruptedException{
              if (monitor == null)
                 monitor = new NullProgressMonitor();

              /*
               * "3" is the total amount of steps, see below monitor.worked(amount)
               */
              monitor.beginTask(CodegenWizardPlugin.getResourceString("generator.generating"), 3);

              try
              {
                 /*
                  * TODO: Introduce a progress monitor interface for CodeGenerationEngine.
                  * Since this monitor here doesn't make much sense, we
                  * should either remove the progress monitor from the CodeGenWizard,
                  * or give a (custom) progress monitor to the generate() method, so
                  * we will be informed by Axis2 about the progress of code generation.  
                  */
                 WSDL2JavaGenerator generator = new WSDL2JavaGenerator(); 
                 monitor.subTask(CodegenWizardPlugin.getResourceString("generator.readingWOM"));
                 AxisService service = generator.getAxisService(wsdlSelectionPage.getFileName());
                 monitor.worked(1);
                 
                 Map optionsMap = generator.fillOptionMap(optionsPage.isAsyncOnlyOn(),
                         									optionsPage.isSyncOnlyOn(),
                         									optionsPage.isServerside(),
                         									optionsPage.isServerXML(),
                         									optionsPage.isGenerateTestCase(),
                         									optionsPage.getGenerateAll(),
                         									optionsPage.getServiceName(),
                         									optionsPage.getPortName(),
                         									optionsPage.getDatabinderName(),
                         									wsdlSelectionPage.getFileName(),
                         									optionsPage.getPackageName(),
                         									optionsPage.getSelectedLanguage(),
                         									outputPage.getOutputLocation());
                 CodeGenConfiguration codegenConfig = new CodeGenConfiguration(service, optionsMap);
                 //set the baseURI
                 codegenConfig.setBaseURI(generator.getBaseUri(wsdlSelectionPage.getFileName()));
                 monitor.worked(1);
                 
                 monitor.subTask(CodegenWizardPlugin.getResourceString("generator.generating"));
                 
                 new CodeGenerationEngine(codegenConfig).generate();
                 
                 //TODO refresh the eclipse project space to show the generated files
                 
                 monitor.worked(1);
              }
              catch (Exception e)
              {
                 ///////////////////////////////
            	  e.printStackTrace();
            	 ///////////////////////////// 
                 throw new InterruptedException(e.getMessage());
              }

              monitor.done();
           }
        };


        /*
         * Start the generation as new Workbench Operation, so the user
         * can see the progress and, if needed, can stop the operation.
         */
        try
        {
           getContainer().run(false, true, op);
        }
        catch (InvocationTargetException e1)
        {
        	/////////////////////////
        	e1.printStackTrace();
        	////////////////////////
            throw new RuntimeException(e1);
        }
        catch (InterruptedException e1)
        {
           throw new RuntimeException(e1);
        }
        catch (Exception e)
        {
           throw new RuntimeException(e);
        }


    }

    private void doFinishJava2WSDL() throws Exception {

        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor) {
                if (monitor == null)
                    monitor = new NullProgressMonitor();

                /*
                 * "2" is the total amount of steps, see below
                 * monitor.worked(amount)
                 */
                monitor.beginTask(CodegenWizardPlugin
                        .getResourceString("generator.generating"), 3);

                try {
                    monitor.worked(1);
                    //fill the option map
                    Map optionsMap = new HashMap();
                    Java2WSDLCommandLineOption option = new Java2WSDLCommandLineOption(
                    		CLASSNAME_OPTION,new String[]{javaSourceSelectionPage.getClassName()});
                    optionsMap.put(CLASSNAME_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		CLASSPATH_OPTION,javaSourceSelectionPage.getClassPathList());
                    optionsMap.put(CLASSPATH_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		TARGET_NAMESPACE_OPTION,
                    		new String[]{java2wsdlOptionsPage.getTargetNamespace()});
                    optionsMap.put(TARGET_NAMESPACE_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		TARGET_NAMESPACE_PREFIX_OPTION,
                    		new String[]{java2wsdlOptionsPage.getTargetNamespacePrefix()});
                    optionsMap.put(TARGET_NAMESPACE_PREFIX_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		SCHEMA_TARGET_NAMESPACE_OPTION,
                    		new String[]{java2wsdlOptionsPage.getSchemaTargetNamespace()});
                    optionsMap.put(SCHEMA_TARGET_NAMESPACE_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION,
                    		new String[]{java2wsdlOptionsPage.getSchemaTargetNamespacePrefix()});
                    optionsMap.put(SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		OUTPUT_LOCATION_OPTION,new String[]{java2wsdlOutputLocationPage.getOutputLocation()});
                    optionsMap.put(OUTPUT_LOCATION_OPTION,option);
                    
                    option = new Java2WSDLCommandLineOption(
                    		OUTPUT_FILENAME_OPTION,new String[]{java2wsdlOutputLocationPage.getOutputWSDLName()});
                    optionsMap.put(OUTPUT_FILENAME_OPTION,option);
                    
                    
                    monitor.worked(1);
                    
                    new Java2WSDLCodegenEngine(optionsMap).generate();
                    
                    monitor.worked(1);
                    
                    
                } catch (Throwable e) {
                	
                	    throw new RuntimeException(e);
                }

                monitor.done();
            }
        };

        try {
            getContainer().run(false, true, op);
        } catch (InvocationTargetException e1) {
            throw new RuntimeException(e1);
        } catch (InterruptedException e1) {
            throw new RuntimeException(CodegenWizardPlugin.
            		getResourceString("general.useraborted.state"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * We will accept the selection in the workbench to see if we can initialize
     * from it.
     * 
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        //do nothing
    }

    /**
     * @return Returns the selectedWizardType.
     */
    public int getSelectedWizardType() {
        return selectedWizardType;
    }

    /**
     * @param selectedWizardType
     *            The selectedWizardType to set.
     */
    public void setSelectedWizardType(int selectedWizardType) {
        this.selectedWizardType = selectedWizardType;
    }
    
    /**
     * Get the selected WSDL from the WSDLselectionpage
     * @return
     */
    public String getWSDLname(){
        return wsdlSelectionPage.getFileName();	
    }
    
    /**
     * populate the options page. Usually done after reloading the WSDL
     *
     */
    public void populateOptions(){
    	optionsPage.populateServiceAndPort();
    }
    
    public void setDefaultNamespaces(String fullyQualifiedClassName){
    	java2wsdlOptionsPage.setNamespaceDefaults(fullyQualifiedClassName);
    }
}