/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis.tool.codegen.eclipse.ui;

import org.apache.axis.tool.codegen.eclipse.plugin.CodegenWizardPlugin;
import org.apache.axis.wsdl.fromJava.Emitter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * -I, --input <argument>input WSDL filename -o, --output <argument>output
 * WSDL filename -l, --location <argument>service location url -P,
 * --portTypeName <argument>portType name (obtained from class-of-portType if
 * not specif ied) -b, --bindingName <argument>binding name (--servicePortName
 * value + "SOAPBinding" if not specified) -S, --serviceElementName <argument>
 * service element name (defaults to --servicePortName value + "Service") -s,
 * --servicePortName <argument>service port name (obtained from --location if
 * not specified ) -n, --namespace <argument>target namespace -p, --PkgtoNS
 * <argument>= <value>package=namespace, name value pairs -m, --methods
 * <argument>space or comma separated list of methods to export -a, --all look
 * for allowed methods in inherited class -w, --outputWsdlMode <argument>output
 * WSDL mode: All, Interface, Implementation -L, --locationImport <argument>
 * location of interface wsdl -N, --namespaceImpl <argument>target namespace
 * for implementation wsdl -O, --outputImpl <argument>output Implementation
 * WSDL filename, setting this causes --o utputWsdlMode to be ignored -i,
 * --implClass <argument>optional class that contains implementation of methods
 * in cl ass-of-portType. The debug information in the class is used to obtain
 * the method parameter names, which are used to set the WSDL part names. -x,
 * --exclude <argument>space or comma separated list of methods not to export
 * -c, --stopClasses <argument>space or comma separated list of class names
 * which will stop inheritance search if --all switch is given -T,
 * --typeMappingVersion <argument>indicate 1.1 or 1.2. The default is 1.1 (SOAP
 * 1.1 JAX-RPC c ompliant 1.2 indicates SOAP 1.1 encoded.) -A, --soapAction
 * <argument>value of the operation's soapAction field. Values are DEFAUL T,
 * OPERATION or NONE. OPERATION forces soapAction to the nam e of the operation.
 * DEFAULT causes the soapAction to be set according to the operation's meta
 * data (usually ""). NONE forces the soapAction to "". The default is DEFAULT.
 * -y, --style <argument>The style of binding in the WSDL, either DOCUMENT,
 * RPC, or W RAPPED. -u, --use <argument>The use of items in the binding,
 * either LITERAL or ENCODED -e, --extraClasses <argument>A space or comma
 * separated list of class names to be added t o the type section. -C,
 * --importSchema A file or URL to an XML Schema that should be physically imp
 * orted into the generated WSDL -X, --classpath additional classpath elements
 */
public class JavaWSDLOptionsPage extends AbstractWizardPage {

    private Text inputWSDLNameTextBox;

    private Text serviceLocationURLTextBox;

    private Text portTypeNameTextBox;

    private Text bindingTextBox;

    private Combo modeSelectionCombo;

    private Combo styleSelectionCombo;


    //TODO need more here

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.tool.codegen.eclipse.ui.AbstractWizardPage#initializeDefaultSettings()
     */
    protected void initializeDefaultSettings() {
        settings.put(PREF_JAVA_INPUT_WSDL_NAME, "");
        settings.put(PREF_JAVA_LOCATION, "http://localhost:8080");
        settings.put(PREF_JAVA_BINDING_NAME, "");
        settings.put(PREF_JAVA_PORTYPE_NAME, "");
        settings.put(PREF_JAVA_MODE_INDEX, 0);
        settings.put(PREF_JAVA_STYLE_INDEX, 0);
    }

    /**
     * @param pageName
     */
    public JavaWSDLOptionsPage() {
        super("page5");

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.tool.codegen.eclipse.ui.CodegenPage#getPageType()
     */
    public int getPageType() {
        return JAVA_2_WSDL_TYPE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 9;

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        Label label = new Label(container, SWT.NULL);
        label.setText(CodegenWizardPlugin
                      .getResourceString("page5.inputwsdl.label"));

        inputWSDLNameTextBox = new Text(container, SWT.BORDER | SWT.SINGLE);
        inputWSDLNameTextBox.setLayoutData(gd);
        inputWSDLNameTextBox.setText(settings.get(PREF_JAVA_INPUT_WSDL_NAME));
        inputWSDLNameTextBox.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                settings.put(PREF_JAVA_INPUT_WSDL_NAME, inputWSDLNameTextBox.getText());
                //dialogChanged();
            }
        });

        label = new Label(container, SWT.NULL);
        label.setText(CodegenWizardPlugin
                      .getResourceString("page5.servicelocation.label"));

        gd = new GridData(GridData.FILL_HORIZONTAL);
        serviceLocationURLTextBox = new Text(container, SWT.BORDER);
        serviceLocationURLTextBox.setLayoutData(gd);
        serviceLocationURLTextBox.setText(settings.get(PREF_JAVA_LOCATION));
        serviceLocationURLTextBox.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                settings.put(PREF_JAVA_LOCATION, serviceLocationURLTextBox.getText());
                //dialogChanged();
            }
        });

        label = new Label(container, SWT.NULL);
        label.setText(CodegenWizardPlugin
                      .getResourceString("page5.binding.label"));

        gd = new GridData(GridData.FILL_HORIZONTAL);
        bindingTextBox = new Text(container, SWT.BORDER);
        bindingTextBox.setLayoutData(gd);
        bindingTextBox.setText(settings.get(PREF_JAVA_BINDING_NAME));
        bindingTextBox.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                settings.put(PREF_JAVA_BINDING_NAME, bindingTextBox.getText());
                //dialogChanged();
            }
        });

        label = new Label(container, SWT.NULL);
        label.setText(CodegenWizardPlugin
                      .getResourceString("page5.porttype.label"));

        gd = new GridData(GridData.FILL_HORIZONTAL);
        portTypeNameTextBox = new Text(container, SWT.BORDER);
        portTypeNameTextBox.setLayoutData(gd);
        portTypeNameTextBox.setText(settings.get(PREF_JAVA_PORTYPE_NAME));
        portTypeNameTextBox.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                settings.put(PREF_JAVA_PORTYPE_NAME, portTypeNameTextBox.getText());
                //dialogChanged();
            }
        });

        // #####################################################
        label = new Label(container, SWT.NULL);
        label
                .setText(CodegenWizardPlugin
                         .getResourceString("page5.mode.label"));

        gd = new GridData(GridData.FILL_HORIZONTAL);
        modeSelectionCombo = new Combo(container, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        modeSelectionCombo.setLayoutData(gd);
        // modeSelectionCombo.
        populateModeCombo();
        modeSelectionCombo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                settings.put(PREF_JAVA_MODE_INDEX, modeSelectionCombo.getSelectionIndex());
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        // #####################################################
        label = new Label(container, SWT.NULL);
        label
                .setText(CodegenWizardPlugin
                         .getResourceString("page5.style.label"));

        gd = new GridData(GridData.FILL_HORIZONTAL);
        styleSelectionCombo = new Combo(container, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        styleSelectionCombo.setLayoutData(gd);
        populateStyleCombo();
        styleSelectionCombo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                settings.put(PREF_JAVA_STYLE_INDEX, styleSelectionCombo.getSelectionIndex());
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });


        setControl(container);

    }

    private void populateModeCombo() {
        modeSelectionCombo.add(WSDL_ALL);
        modeSelectionCombo.add(WSDL_INTERFACE_ONLY);
        modeSelectionCombo.add(WSDL_IMPLEMENTATION_ONLY);

        modeSelectionCombo.select(settings.getInt(PREF_JAVA_MODE_INDEX));
    }

    private void populateStyleCombo() {
        styleSelectionCombo.add(WSDL_STYLE_DOCUMENT);
        styleSelectionCombo.add(WSDL_STYLE_RPC);
        styleSelectionCombo.add(WSDL_STYLE_WRAPPED);

        styleSelectionCombo.select(settings.getInt(PREF_JAVA_STYLE_INDEX));
    }

    public int getMode() {
        String selectedOption = modeSelectionCombo.getItem(modeSelectionCombo.getSelectionIndex());
        if (WSDL_ALL.equals(selectedOption)) {
            return Emitter.MODE_ALL;
        } else if (WSDL_INTERFACE_ONLY.equals(selectedOption)) {
            return Emitter.MODE_INTERFACE;
        } else if (WSDL_IMPLEMENTATION_ONLY.equals(selectedOption)) {
            return Emitter.MODE_IMPLEMENTATION;
        } else {
            throw new RuntimeException("Unknown Exception");
        }
    }


    public String getStyle() {
        return this.styleSelectionCombo.getItem(styleSelectionCombo.getSelectionIndex()).toUpperCase();
    }

    public String getLocationURL() {
        return this.serviceLocationURLTextBox.getText();
    }

    public String getInputWSDLName() {
        return this.inputWSDLNameTextBox.getText();
    }

    public String getPortypeName() {
        return this.portTypeNameTextBox.getText();
    }

    public String getBindingName() {
        return this.bindingTextBox.getText();
    }

    private String getgetClassFileLocation() {
        return null;
    }


}
