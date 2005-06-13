/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.apache.axis.tool.codegen.eclipse.ui;

import org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin;
import org.apache.axis.tool.codegen.eclipse.util.UIConstants;
import org.apache.axis.wsdl.util.URLProcessor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Options Page lets the user change general settings on the code generation. It is used in the CodegenWizardPlugin,
 * CodeGenWizard.
 * 
 */
public class OptionsPage extends WizardPage implements UIConstants
{
   /**
    * Position in the combox for choosing the target programming language. Default is 0
    */
   private static final String PREF_LANGUAGE_INDEX = "PREF_LANGUAGE_INDEX";

   /**
    * Three radio buttons: Generate Code for Sync calls, Async and Both. Both is default.
    */
   private static final String PREF_RADIO_SYNC_AND_ASYNC = "PREF_RADIO_SYNC_AND_ASYNC";

   /**
    * Three radio buttons: Generate Code for Sync calls, Async and Both. Both is default.
    */
   private static final String PREF_RADIO_SYNC_ONLY = "PREF_RADIO_SYNC_ONLY";

   /**
    * Three radio buttons: Generate Code for Sync calls, Async and Both. Both is default.
    */
   private static final String PREF_RADIO_ASYNC_ONLY = "PREF_RADIO_ASYNC_ONLY";

   /**
    * Specifies the full qualified package name for the generated source code.
    */
   private static final String PREF_PACKAGE_NAME = "PREF_PACKAGE_NAME";

   /**
    * A boolean value whether JUnit test classes are generated or not.
    */
   private static final String PREF_CHECK_GENERATE_TESTCASE = "PREF_CHECK_GENERATE_TESTCASE";

   /**
    * A boolean value whether the server-side skeletons are generated or not
    */
   private static final String PREF_CHECK_GENERATE_SERVERSIDE = "PREF_CHECK_GENERATE_SERVERSIDE";

   /**
    * A boolean value whether the server-side configuration file for Axis2 (server.xml) will be generated or not.
    */
   private static final String PREF_CHECK_GENERATE_SERVERCONFIG = "PREF_CHECK_GENERATE_SERVERCONFIG";

   /**
    * Selection list for target languages
    */
   private Combo languageSelectionComboBox;

   /**
    * A radio button to enable/disable code generation for synchronous and asynchronous calls.
    */
   private Button syncAndAsyncRadioButton;

   /**
    * A radio button to choose "synchronous only" code generation
    */
   private Button syncOnlyRadioButton;

   /**
    * A radio button to choose "asynchronous only" code generation
    */
   private Button asyncOnlyRadioButton;

   /**
    * Label holding the full qualified package name for generated code
    */
   private Text packageText;

   /**
    * Checkbox to enable server-side skeleton code generation. If enabled, generates an empty implementation of the
    * service
    */
   private Button serverSideCheckBoxButton;

   /**
    * Checkbox to enable the generation of test case classes for the generated implementation of the webservice.
    */
   private Button testCaseCheckBoxButton;

   /**
    * Checkbox to enable the generation of a default server.xml configuration file
    */
   private Button serverXMLCheckBoxButton;

   /**
    * Saves the settings in the plugin's default store, so the user doesn't have to type them in all over again next
    * time he uses the wizard
    */
   private IDialogSettings settings;

   /**
    * Creates the page and initialize some settings
    */
   public OptionsPage()
   {
      super(CodegenWizardPlugin.getResourceString("page2.name"));
      setTitle(org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin.getResourceString("page2.title"));
      setDescription(CodegenWizardPlugin.getResourceString("page2.desc"));
      setImageDescriptor(CodegenWizardPlugin.getWizardImageDescriptor());

      /*
       * Get the settings for this page. If there is no section in the Plugin's settings for this OptionsPage, create a
       * new section
       */
      IDialogSettings rootSettings = CodegenWizardPlugin.getDefault().getDialogSettings();
      IDialogSettings section = rootSettings.getSection(this.getClass().getName());
      if (section == null)
      {
         settings = rootSettings.addNewSection(this.getClass().getName());
         initializeDefaultSettings();
      }
      else
      {
         settings = section;
      }
   }

   /**
    * Sets the default values for the Options page
    * 
    * @param settings2 the settings store to save the values to
    */
   private void initializeDefaultSettings()
   {
      settings.put(OptionsPage.PREF_CHECK_GENERATE_SERVERCONFIG, false);
      settings.put(OptionsPage.PREF_CHECK_GENERATE_SERVERSIDE, false);
      settings.put(OptionsPage.PREF_CHECK_GENERATE_TESTCASE, false);
      settings.put(OptionsPage.PREF_LANGUAGE_INDEX, 0);
      settings.put(OptionsPage.PREF_PACKAGE_NAME, "org.example.webservice");
      settings.put(OptionsPage.PREF_RADIO_ASYNC_ONLY, false);
      settings.put(OptionsPage.PREF_RADIO_SYNC_AND_ASYNC, true);
      settings.put(OptionsPage.PREF_RADIO_SYNC_ONLY, false);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
    */
   public void createControl(Composite parent)
   {

      Composite container = new Composite(parent, SWT.NULL);
      GridLayout layout = new GridLayout();
      container.setLayout(layout);
      layout.numColumns = 3;
      layout.verticalSpacing = 9;

      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.horizontalSpan = 2;

      Label label = new Label(container, SWT.NULL);
      label.setText(CodegenWizardPlugin.getResourceString("page2.language.caption"));

      languageSelectionComboBox = new Combo(container, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
      // fill the combo
      this.fillLanguageCombo();
      languageSelectionComboBox.setLayoutData(gd);
      languageSelectionComboBox.select(settings.getInt(PREF_LANGUAGE_INDEX));
      languageSelectionComboBox.addSelectionListener(new SelectionListener()
      {
         public void widgetSelected(SelectionEvent e)
         {
            settings.put(PREF_LANGUAGE_INDEX, languageSelectionComboBox.getSelectionIndex());
         }

         public void widgetDefaultSelected(SelectionEvent e)
         {
         }
      });

      syncAndAsyncRadioButton = new Button(container, SWT.RADIO);
      syncAndAsyncRadioButton.setText(CodegenWizardPlugin.getResourceString("page2.syncAsync.caption"));
      syncAndAsyncRadioButton.setSelection(settings.getBoolean(PREF_RADIO_SYNC_AND_ASYNC));
      syncAndAsyncRadioButton.addSelectionListener(new SelectionListener()
      {
         public void widgetSelected(SelectionEvent e)
         {
            settings.put(PREF_RADIO_SYNC_AND_ASYNC, syncAndAsyncRadioButton.getSelection());
         }

         public void widgetDefaultSelected(SelectionEvent e)
         {
         }
      });

      syncOnlyRadioButton = new Button(container, SWT.RADIO);
      syncOnlyRadioButton.setText(CodegenWizardPlugin.getResourceString("page2.sync.caption"));
      syncOnlyRadioButton.setSelection(settings.getBoolean(PREF_RADIO_SYNC_ONLY));
      syncOnlyRadioButton.addSelectionListener(new SelectionListener()
      {
         public void widgetSelected(SelectionEvent e)
         {
            settings.put(PREF_RADIO_SYNC_ONLY, syncOnlyRadioButton.getSelection());
         }

         public void widgetDefaultSelected(SelectionEvent e)
         {
         }
      });

      asyncOnlyRadioButton = new Button(container, SWT.RADIO);
      asyncOnlyRadioButton.setText(org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin
         .getResourceString("page2.async.caption"));
      asyncOnlyRadioButton.setSelection(settings.getBoolean(PREF_RADIO_ASYNC_ONLY));
      asyncOnlyRadioButton.addSelectionListener(new SelectionListener()
      {
         public void widgetSelected(SelectionEvent e)
         {
            settings.put(PREF_RADIO_ASYNC_ONLY, asyncOnlyRadioButton.getSelection());
         }

         public void widgetDefaultSelected(SelectionEvent e)
         {
         }
      });

      label = new Label(container, SWT.NULL);
      label.setText(CodegenWizardPlugin.getResourceString("page2.package.caption"));

      packageText = new Text(container, SWT.BORDER);
      gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.horizontalSpan = 2;

      packageText.setLayoutData(gd);
      String packageName;
      String storedPackageName = settings.get(PREF_PACKAGE_NAME);
      if (storedPackageName.equals(""))
      {
         packageName = URLProcessor.getNameSpaceFromURL("");
      }
      else
      {
         packageName = storedPackageName;
      }
      packageText.setText(packageName); // get this text from the URLProcessor
      packageText.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            settings.put(PREF_PACKAGE_NAME, packageText.getText());
         }
      });


      gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.horizontalSpan = 3;
      testCaseCheckBoxButton = new Button(container, SWT.CHECK);
      testCaseCheckBoxButton.setLayoutData(gd);
      testCaseCheckBoxButton.setText(org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin
         .getResourceString("page2.testcase.caption"));
      testCaseCheckBoxButton.setSelection(settings.getBoolean(PREF_CHECK_GENERATE_TESTCASE));
      testCaseCheckBoxButton.addSelectionListener(new SelectionListener()
      {
         public void widgetSelected(SelectionEvent e)
         {
            settings.put(PREF_CHECK_GENERATE_TESTCASE, testCaseCheckBoxButton.getEnabled());
         }

         public void widgetDefaultSelected(SelectionEvent e)
         {
         }
      });

      serverSideCheckBoxButton = new Button(container, SWT.CHECK);
      serverSideCheckBoxButton.setText(CodegenWizardPlugin.getResourceString("page2.serverside.caption"));
      serverSideCheckBoxButton.setSelection(settings.getBoolean(PREF_CHECK_GENERATE_SERVERSIDE));
      serverSideCheckBoxButton.addSelectionListener(new SelectionListener()
      {
         public void widgetSelected(SelectionEvent e)
         {
            handleServersideSelection();
            settings.put(PREF_CHECK_GENERATE_SERVERSIDE, serverSideCheckBoxButton.getEnabled());
         }

         public void widgetDefaultSelected(SelectionEvent e)
         {
         }
      });

      serverXMLCheckBoxButton = new Button(container, SWT.CHECK);
      serverXMLCheckBoxButton.setSelection(settings.getBoolean(PREF_CHECK_GENERATE_SERVERCONFIG));
      serverXMLCheckBoxButton.setText(CodegenWizardPlugin.getResourceString("page2.serviceXML.caption"));
      serverXMLCheckBoxButton.addSelectionListener(new SelectionListener()
      {
         public void widgetSelected(SelectionEvent e)
         {
            settings.put(PREF_CHECK_GENERATE_SERVERCONFIG, serverXMLCheckBoxButton.getEnabled());
         }

         public void widgetDefaultSelected(SelectionEvent e)
         {
         }
      });

      /*
       * Check the state of server-side selection, so we can enable/disable
       * the serverXML checkbox button.
       */
      handleServersideSelection();

      
      setControl(container);
      setPageComplete(true);

   }

   /**
    * Fill the combo with proper language names
    * 
    */
   private void fillLanguageCombo()
   {

      languageSelectionComboBox.add(JAVA);
      languageSelectionComboBox.add(C_SHARP);
      languageSelectionComboBox.add(C_PLUS_PLUS);

      languageSelectionComboBox.select(0);
   }

   /**
    * Validates the status of the server-side checkbox, and enables/disables
    * the generation checkbox for XML configuration file
    */
   private void handleServersideSelection()
   {
      if (this.serverSideCheckBoxButton.getSelection())
      {
         this.serverXMLCheckBoxButton.setEnabled(true);
      }
      else
      {
         this.serverXMLCheckBoxButton.setEnabled(false);
      }
   }

   /**
    * Get the selected language
    * 
    * @return a string containing the name of the target language
    */
   public String getSelectedLanguage()
   {
      return languageSelectionComboBox.getItem(languageSelectionComboBox.getSelectionIndex());
   }

   /**
    * the async only status
    * 
    * @return true if "Generate asynchronous code only" is checked
    */
   public boolean isAsyncOnlyOn()
   {
      return asyncOnlyRadioButton.getSelection();
   }

   /**
    * the sync only status
    * 
    * @return true if "Generate synchronous code only" is checked
    */
   public boolean isSyncOnlyOn()
   {
      return syncOnlyRadioButton.getSelection();
   }

   /**
    * return the package name
    * 
    * @return a string containing the package name to use for code generation
    */
   public String getPackageName()
   {
      return this.packageText.getText();
   }

   /**
    * The serverside status
    * 
    * @return true if "Generate Server-Side" is checked
    */
   public boolean isServerside()
   {
      return this.serverSideCheckBoxButton.getSelection();
   }

   /**
    * 
    * @return true if "Generate XML configuration file" is checked
    */
   public boolean isServerXML()
   {
      if (this.serverXMLCheckBoxButton.isEnabled())
         return this.serverXMLCheckBoxButton.getSelection();
      else
         return false;
   }

   /**
    * 
    * @return true if "Generate test case" is checked
    */
   public boolean isGenerateTestCase()
   {
      return this.testCaseCheckBoxButton.getSelection();
   }
}
