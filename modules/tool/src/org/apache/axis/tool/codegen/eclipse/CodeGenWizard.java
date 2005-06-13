
package org.apache.axis.tool.codegen.eclipse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.wsdl.WSDLException;

import org.apache.axis.tool.codegen.eclipse.ui.OptionsPage;
import org.apache.axis.tool.codegen.eclipse.ui.OutputPage;
import org.apache.axis.tool.codegen.eclipse.ui.WSDLFileSelectionPage;
import org.apache.axis.tool.codegen.eclipse.util.UIConstants;
import org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin;
import org.apache.axis.wsdl.builder.WOMBuilderFactory;
import org.apache.axis.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis.wsdl.codegen.CommandLineOption;
import org.apache.axis.wsdl.codegen.CommandLineOptionConstants;
import org.apache.wsdl.WSDLDescription;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * The main wizard for the codegen wizard
 */

public class CodeGenWizard extends Wizard implements INewWizard
{
   private WSDLFileSelectionPage page1;

   private OptionsPage page2;

   private OutputPage page3;

   private ISelection selection;

   private boolean canFinish = false;

   /**
    * Constructor for CodeGenWizard.
    */
   public CodeGenWizard()
   {
      super();
      setNeedsProgressMonitor(true);
      this.setWindowTitle(org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin
         .getResourceString("general.name"));
   }

   /**
    * Adding the page to the wizard.
    */

   public void addPages()
   {
      page1 = new WSDLFileSelectionPage(selection);
      addPage(page1);
      page2 = new OptionsPage();
      addPage(page2);
      page3 = new OutputPage();
      addPage(page3);

   }

   /**
    * This method is called when 'Finish' button is pressed in the wizard. We will create an operation and run it using
    * wizard as execution context.
    */
   public boolean performFinish()
   {
      try
      {
         // getContainer().run(true, false, op);
         doFinish();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         MessageDialog.openError(getShell(), CodegenWizardPlugin.getResourceString("general.Error"), e.getMessage());
         return false;
      }
      MessageDialog.openInformation(this.getShell(), CodegenWizardPlugin.getResourceString("general.name"),
         CodegenWizardPlugin.getResourceString("wizard.success"));
      return true;
   }

   /**
    * The worker method, generates the code itself.
    */
   private void doFinish()
   {

      WorkspaceModifyOperation op = new WorkspaceModifyOperation()
      {
         protected void execute(IProgressMonitor monitor)
         {
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
               monitor.subTask(CodegenWizardPlugin.getResourceString("generator.readingWOM"));
               WSDLDescription wom = getWOM(page1.getFileName());
               monitor.worked(1);
               Map optionsMap = fillOptionMap();
               CodeGenConfiguration codegenConfig = new CodeGenConfiguration(wom, optionsMap);
               monitor.worked(1);
               monitor.subTask(CodegenWizardPlugin.getResourceString("generator.generating"));
               new CodeGenerationEngine(codegenConfig).generate();
               monitor.worked(1);
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
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
         e1.printStackTrace();
      }
      catch (InterruptedException e1)
      {
         /*
          * Thrown when the user aborts the progress
          */
         e1.printStackTrace();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Creates a list of parameters for the code generator based on the decisions made by the user on the OptionsPage
    * (page2). For each setting, there is a Command-Line option for the Axis2 code generator.
    * 
    * @return a Map with keys from CommandLineOptionConstants with the values entered by the user on the Options Page.
    */
   private Map fillOptionMap()
   {
      Map optionMap = new HashMap();

      optionMap.put(CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION, new CommandLineOption(
         CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION, getStringArray(page1.getFileName())));

      if (page2.isAsyncOnlyOn())
      {
         optionMap.put(CommandLineOptionConstants.CODEGEN_ASYNC_ONLY_OPTION, new CommandLineOption(
            CommandLineOptionConstants.CODEGEN_ASYNC_ONLY_OPTION, new String[0]));
      }
      if (page2.isSyncOnlyOn())
      {
         optionMap.put(CommandLineOptionConstants.CODEGEN_SYNC_ONLY_OPTION, new CommandLineOption(
            CommandLineOptionConstants.CODEGEN_SYNC_ONLY_OPTION, new String[0]));
      }
      optionMap.put(CommandLineOptionConstants.PACKAGE_OPTION, new CommandLineOption(
         CommandLineOptionConstants.PACKAGE_OPTION, getStringArray(page2.getPackageName())));
      optionMap.put(CommandLineOptionConstants.STUB_LANGUAGE_OPTION, new CommandLineOption(
         CommandLineOptionConstants.STUB_LANGUAGE_OPTION, getStringArray(mapLanguagesWithCombo(page2
            .getSelectedLanguage()))));
      optionMap.put(CommandLineOptionConstants.OUTPUT_LOCATION_OPTION, new CommandLineOption(
         CommandLineOptionConstants.OUTPUT_LOCATION_OPTION, getStringArray(page3.getOutputLocation())));
      if (page2.isServerside())
      {
         optionMap.put(CommandLineOptionConstants.SERVER_SIDE_CODE_OPTION, new CommandLineOption(
            CommandLineOptionConstants.SERVER_SIDE_CODE_OPTION, new String[0]));

         if (page2.isServerXML())
         {
            optionMap.put(CommandLineOptionConstants.GENERATE_SERVICE_DESCRIPTION_OPTION, new CommandLineOption(
               CommandLineOptionConstants.GENERATE_SERVICE_DESCRIPTION_OPTION, new String[0]));
         }
      }
      if (page2.isGenerateTestCase())
      {
         optionMap.put(CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION, new CommandLineOption(
            CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION, new String[0]));
      }
      // System.out.println(page3.getOutputLocation());
      return optionMap;
   }

   /**
    * Maps a string containing the name of a language to a constant defined in CommandLineOptionConstants.LanguageNames
    * 
    * @param UILangValue a string containg a language, e.g. "java", "cs", "cpp" or "vb"
    * @return a normalized string constant
    */
   private String mapLanguagesWithCombo(String UILangValue)
   {
      if (UIConstants.JAVA.equals(UILangValue))
      {
         return CommandLineOptionConstants.LanguageNames.JAVA;
      }
      else if (UIConstants.C_SHARP.equals(UILangValue))
      {
         return CommandLineOptionConstants.LanguageNames.C_SHARP;
      }
      else if (UIConstants.C_PLUS_PLUS.equals(UILangValue))
      {
         return CommandLineOptionConstants.LanguageNames.C_PLUS_PLUS;
      }
      else
      {
         return null;
      }
   }

   /**
    * We will accept the selection in the workbench to see if we can initialize from it.
    * 
    * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
    */
   public void init(IWorkbench workbench, IStructuredSelection selection)
   {
      this.selection = selection;
   }

   /**
    * Reads the WSDL Object Model from the given location.
    * 
    * @param wsdlLocation the filesystem location (full path) of the WSDL file to read in.
    * @return the WSDLDescription object containing the WSDL Object Model of the given WSDL file
    * @throws WSDLException when WSDL File is invalid
    * @throws IOException on errors reading the WSDL file
    */
   private WSDLDescription getWOM(String wsdlLocation) throws WSDLException, IOException
   {
      InputStream in = new FileInputStream(new File(wsdlLocation));
      return WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11).build(in);
   }

   /**
    * Converts a single String into a String Array
    * 
    * @param value a single string
    * @return an array containing only one element
    */
   private String[] getStringArray(String value)
   {
      String[] values = new String[1];
      values[0] = value;
      return values;
   }
}