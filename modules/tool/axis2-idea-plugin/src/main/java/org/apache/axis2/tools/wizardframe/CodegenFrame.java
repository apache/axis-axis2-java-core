/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.tools.wizardframe;

import org.apache.axis2.tools.bean.WsdlgenBean;
import org.apache.axis2.tools.bean.CodegenBean;
import org.apache.axis2.tools.idea.FirstPanel;
import org.apache.axis2.tools.component.Utilities;
import org.apache.axis2.tools.component.WizardPanel;

/**
 * codeaFrame class
 */

public class CodegenFrame extends WizardFrame{
    public static final int PANEL_CHOOSER = 0;
    public static final int PANEL_FIRST_A = 1;    //
    public static final int PANEL_FIRST_B = 2;
    public static final int PANEL_OPTION_A =2;
    public static final int PANEL_OPTION_B = 3;
    public static final int PANEL_LAST_A = 3;
    public static final int PANEL_LAST_B = 6;

    private WsdlgenBean wsdlgenBean;
    private CodegenBean codegenBean;

    public CodegenFrame() {
        super();
        init();
    }

    private void init() {
        this.setTitle("Axis2 Codegen Wizard");

        WizardPanel panel = null;

        wsdlgenBean= new WsdlgenBean();
        codegenBean = new CodegenBean();
       /*
        panel = new ChooserPanel(getWizardComponents());
        getWizardComponents().addWizardPanel(PANEL_CHOOSER , panel);

        panel = new FirstPanel(getWizardComponents(),codegenBean);
        getWizardComponents().addWizardPanel(PANEL_FIRST_A , panel);

        panel = new J2WFristWizardPanel(getWizardComponents(),wsdlgenBean );
        getWizardComponents().addWizardPanel(PANEL_FIRST_B, panel);

        panel = new SecondPanel(getWizardComponents(),codegenBean);
        getWizardComponents().addWizardPanel(PANEL_OPTION_A , panel);

        panel = new J2WOptionWizardPanel(getWizardComponents(),wsdlgenBean);
        getWizardComponents().addWizardPanel(PANEL_OPTION_B , panel);

        panel = new OutPutPanel(getWizardComponents(),codegenBean);
        getWizardComponents().addWizardPanel(PANEL_LAST_A , panel);

        panel = new J2WLocationWizardPanel(getWizardComponents(),wsdlgenBean);
        getWizardComponents().addWizardPanel(PANEL_LAST_B , panel);*/

        setSize(550, 200);
        Utilities.centerComponentOnScreen(this);

    }
}
