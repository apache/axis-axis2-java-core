package org.apache.axis.tool.codegen.eclipse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.wsdl.WSDLException;

import org.apache.axis.tool.codegen.eclipse.ui.OptionsPage;
import org.apache.axis.tool.codegen.eclipse.ui.OutputPage;
import org.apache.axis.tool.codegen.eclipse.ui.WSDLFileSelectionPage;
import org.apache.axis.tool.codegen.eclipse.util.UIConstants;
import org.apache.axis.tool.codegen.eclipse.plugin.*;
import org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin;
import org.apache.axis.wsdl.builder.WOMBuilderFactory;
import org.apache.axis.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis.wsdl.codegen.CommandLineOption;
import org.apache.axis.wsdl.codegen.CommandLineOptionConstants;
import org.apache.wsdl.WSDLDescription;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * The main wizard for the codegen wizard
 */

public class CodeGenWizard extends Wizard implements INewWizard {
    private WSDLFileSelectionPage page1;

    private OptionsPage page2;

    private OutputPage page3;

    private ISelection selection;

    private boolean canFinish = false;

    /**
     * Constructor for CodeGenWizard.
     */
    public CodeGenWizard() {
        super();
        setNeedsProgressMonitor(true);
        this.setWindowTitle(org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin
                .getResourceString("general.name"));
    }

    /**
     * Adding the page to the wizard.
     */

    public void addPages() {
        page1 = new WSDLFileSelectionPage(selection);
        addPage(page1);
        page2 = new OptionsPage();
        addPage(page2);
        page3 = new OutputPage();
        addPage(page3);

    }

    /**
     * This method is called when 'Finish' button is pressed in the wizard. We
     * will create an operation and run it using wizard as execution context.
     */
    public boolean performFinish() {

        try {
            // getContainer().run(true, false, op);
            doFinish();
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openError(getShell(), CodegenWizardPlugin
                    .getResourceString("general.Error"), e.getMessage());
            return false;
        }
        MessageDialog.openInformation(this.getShell(), CodegenWizardPlugin
                .getResourceString("general.name"), CodegenWizardPlugin
                .getResourceString("wizard.success"));
        return true;
    }

    /**
     * The worker method.
     */

    private void doFinish() {
  
        try {
            WSDLDescription wom = this.getWOM(page1.getFileName());
            Map optionsMap = fillOptionMap();
            CodeGenConfiguration codegenConfig = new CodeGenConfiguration(wom,
                    optionsMap);
            new CodeGenerationEngine(codegenConfig).generate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     *  
     */
    private Map fillOptionMap() {
        Map optionMap = new HashMap();

        optionMap.put(CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION,
                        getStringArray(page1.getFileName())));

        if (page2.isAsyncOnlyOn()) {
            optionMap
                    .put(
                            CommandLineOptionConstants.CODEGEN_ASYNC_ONLY_OPTION,
                            new CommandLineOption(
                                    CommandLineOptionConstants.CODEGEN_ASYNC_ONLY_OPTION,
                                    new String[0]));
        }
        if (page2.isSyncOnlyOn()) {
            optionMap
                    .put(
                            CommandLineOptionConstants.CODEGEN_SYNC_ONLY_OPTION,
                            new CommandLineOption(
                                    CommandLineOptionConstants.CODEGEN_SYNC_ONLY_OPTION,
                                    new String[0]));
        }
        optionMap.put(CommandLineOptionConstants.PACKAGE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.PACKAGE_OPTION,
                        getStringArray(page2.getPackageName())));
        optionMap.put(CommandLineOptionConstants.STUB_LANGUAGE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.STUB_LANGUAGE_OPTION,
                        getStringArray(mapLanguagesWithCombo(page2
                                .getSelectedLanguage()))));
        optionMap.put(CommandLineOptionConstants.OUTPUT_LOCATION_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.OUTPUT_LOCATION_OPTION,
                        getStringArray(page3.getOutputLocation())));
        if (page2.isServerside()) {
            optionMap.put(CommandLineOptionConstants.SERVER_SIDE_CODE_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.SERVER_SIDE_CODE_OPTION,
                            new String[0]));

            if (page2.isServerXML()) {
                optionMap
                        .put(
                                CommandLineOptionConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
                                new CommandLineOption(
                                        CommandLineOptionConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
                                        new String[0]));
            }
        }
        if (page2.isGenerateTestCase()){
            optionMap
            .put(
                    CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
                    new CommandLineOption(
                            CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
                            new String[0])); 
        }
        //System.out.println(page3.getOutputLocation());
        return optionMap;
    }

    private String mapLanguagesWithCombo(String UILangValue) {
        if (UIConstants.JAVA.equals(UILangValue)) {
            return CommandLineOptionConstants.LanguageNames.JAVA;
        } else if (UIConstants.C_SHARP.equals(UILangValue)) {
            return CommandLineOptionConstants.LanguageNames.C_SHARP;
        } else if (UIConstants.C_PLUS_PLUS.equals(UILangValue)) {
            return CommandLineOptionConstants.LanguageNames.C_PLUS_PLUS;
        } else {
            return null;
        }
    }

    /**
     * We will accept the selection in the workbench to see if we can initialize
     * from it.
     * 
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

    private WSDLDescription getWOM(String wsdlLocation) throws WSDLException,
            IOException {
        InputStream in = new FileInputStream(new File(wsdlLocation));
        return WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11).build(in);
    }

    private String[] getStringArray(String value) {
        String[] values = new String[1];
        values[0] = value;
        return values;
    }
}