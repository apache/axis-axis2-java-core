/** (C) Copyright 2005 Hewlett-Packard Development Company, LP

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 For more information: www.smartfrog.org

 */
package org.apache.axis2.fault;

/**
 * Representation of a FaultReason; a language specific reason for a fault.
 */

public class FaultReason {

    /**
     * env:reasontext
     */
    private String text;

    /**
     * Language of the reason.
     * xml:lang="en" "en-GB" or just ""
     */
    private String language = "";

    public FaultReason(String text, String language) {
        this.text = text;
        this.language = language;
    }

    public FaultReason() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return the text value
     */
    public String toString() {
        return text;
    }
}
