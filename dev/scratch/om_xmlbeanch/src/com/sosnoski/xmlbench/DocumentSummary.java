/*
 * Copyright (c) 2000-2001 Sosnoski Software Solutions, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.sosnoski.xmlbench;

/**
 * Document summary information. This includes several count values
 * characteristic of a document, allowing simple consistency checks across
 * different representations of the document.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public class DocumentSummary
{
	/** Number of elements. */
	private int m_elementCount;

	/** Number of content text segments. */
	private int m_contentCount;

	/** Number of attributes. */
	private int m_attributeCount;

	/** Number of characters of content text. */
	private int m_textCharCount;

	/** Number of characters of attribute data. */
	private int m_attrCharCount;

	/**
	 * Reset count values.
	 */

	public void reset() {
		m_elementCount = 0;
		m_contentCount = 0;
		m_attributeCount = 0;
		m_textCharCount = 0;
		m_attrCharCount = 0;
	}

	/**
	 * Get element count.
	 *
	 * @return number of elements
	 */

	public int getElementCount() {
		return m_elementCount;
	}

	/**
	 * Get content segment count.
	 *
	 * @return number of content segments
	 */

	public int getContentCount() {
		return m_contentCount;
	}

	/**
	 * Get attribute count.
	 *
	 * @return number of attributes
	 */

	public int getAttributeCount() {
		return m_attributeCount;
	}

	/**
	 * Get text content character count.
	 *
	 * @return number of text characters
	 */

	public int getTextCharCount() {
		return m_textCharCount;
	}

	/**
	 * Get attribute value character count.
	 *
	 * @return number of attribute value characters
	 */

	public int getAttrCharCount() {
		return m_attrCharCount;
	}

	/**
	 * Add to element count.
	 *
	 * @param count value to be added to element count
	 */

	public void addElements(int count) {
		m_elementCount += count;
	}

	/**
	 * Count attribute. Increments the attribute count by one and adds the
	 * supplied character count to the attribute data length.
	 *
	 * @param length attribute value text length
	 */

	public void addAttribute(int length) {
		m_attributeCount++;
		m_attrCharCount += length;
	}

	/**
	 * Count content text segment. Increments the content segment count by one
	 * and adds the supplied character count to the content text length.
	 *
	 * @param length attribute value text length
	 */

	public void addContent(int length) {
		m_contentCount++;
		m_textCharCount += length;
	}

	/**
	 * Check if object is equal to this one.
	 *
	 * @param obj object to be compared
	 * @return <code>true</code> if the values match, <code>false</code>
	 * if not
	 */

	public boolean equals(Object obj) {
		if (obj instanceof DocumentSummary) {
			DocumentSummary comp = (DocumentSummary)obj;
			return m_elementCount == comp.m_elementCount &&
				m_contentCount == comp.m_contentCount &&
				m_attributeCount == comp.m_attributeCount &&
				m_textCharCount == comp.m_textCharCount &&
				m_attrCharCount == comp.m_attrCharCount;
		} else {
			return false;
		}
	}

	/**
	 * Check if object structure is equal to this one. This comparison ignores
	 * the text content segment count and total character length, since that
	 * can be changed by output formatting.
	 *
	 * @param obj object to be compared
	 * @return <code>true</code> if the values match, <code>false</code>
	 * if not
	 */

	public boolean structureEquals(Object obj) {
		if (obj instanceof DocumentSummary) {
			DocumentSummary comp = (DocumentSummary)obj;
			return m_elementCount == comp.m_elementCount &&
				m_attributeCount == comp.m_attributeCount &&
				m_attrCharCount == comp.m_attrCharCount;
		} else {
			return false;
		}
	}
}

