<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:sch="http://www.ascc.net/xml/schematron"
                xmlns:iso="http://purl.oclc.org/dsdl/schematron"
                xmlns:cml="http://www.xml-cml.org/schema"
                version="2.0"
                cml:dummy-for-xmlns="">

<!--PHASES-->


<!--PROLOG-->
<xsl:output xmlns:svrl="http://purl.oclc.org/dsdl/svrl" method="xml"
               omit-xml-declaration="no"
               standalone="yes"
               indent="yes"/>

   <!--KEYS-->


<!--DEFAULT RULES-->


<!--MODE: SCHEMATRON-FULL-PATH-->
<xsl:template match="*|@*" mode="schematron-get-full-path">
      <xsl:apply-templates select="parent::*" mode="schematron-get-full-path"/>
      <xsl:text>/</xsl:text>
      <xsl:choose>
         <xsl:when test="namespace-uri()=''">
            <xsl:value-of select="name()"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:text>*:</xsl:text>
            <xsl:value-of select="local-name()"/>
            <xsl:text>[namespace-uri()='</xsl:text>
            <xsl:value-of select="namespace-uri()"/>
            <xsl:text>']</xsl:text>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="preceding"
                    select="count(preceding-sibling::*[local-name()=local-name(current())                                   and namespace-uri() = namespace-uri(current())])"/>
      <xsl:text>[</xsl:text>
      <xsl:value-of select="1+ $preceding"/>
      <xsl:text>]</xsl:text>
   </xsl:template>
   <xsl:template match="@*" mode="schematron-get-full-path">
      <xsl:apply-templates select="parent::*" mode="schematron-get-full-path"/>@*[local-name()='schema' and namespace-uri()='http://purl.oclc.org/dsdl/schematron']</xsl:template>

   <!--MODE: GENERATE-ID-FROM-PATH -->
<xsl:template match="/" mode="generate-id-from-path"/>
   <xsl:template match="text()" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.text-', 1+count(preceding-sibling::text()), '-')"/>
   </xsl:template>
   <xsl:template match="comment()" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.comment-', 1+count(preceding-sibling::comment()), '-')"/>
   </xsl:template>
   <xsl:template match="processing-instruction()" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.processing-instruction-', 1+count(preceding-sibling::processing-instruction()), '-')"/>
   </xsl:template>
   <xsl:template match="@*" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.@', name())"/>
   </xsl:template>
   <xsl:template match="*" mode="generate-id-from-path" priority="-0.5">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:text>.</xsl:text>
      <xsl:choose>
         <xsl:when test="count(. | ../namespace::*) = count(../namespace::*)">
            <xsl:value-of select="concat('.namespace::-',1+count(namespace::*),'-')"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="concat('.',name(),'-',1+count(preceding-sibling::*[name()=name(current())]),'-')"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <!--Strip characters--><xsl:template match="text()" priority="-1"/>

   <!--SCHEMA METADATA-->
<xsl:template match="/">
      <svrl:schematron-output xmlns:svrl="http://purl.oclc.org/dsdl/svrl" title="CMLLite schematron file"
                              schemaVersion="ISO19757-3">
         <svrl:text>
            <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
                 xmlns:h="http://www.w3.org/1999/xhtml"
                 xmlns:dc="http://purl.org/dc/elements/1.1/"
                 xmlns:fn="http://www.w3.org/2005/02/xpath-functions">Checks that the elements and attributes being used conform to the structure and subset as agreed upon by MS and UCC.</h:p>
            <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
                 xmlns:h="http://www.w3.org/1999/xhtml"
                 xmlns:dc="http://purl.org/dc/elements/1.1/"
                 xmlns:fn="http://www.w3.org/2005/02/xpath-functions">
               <h:b>NOTE</h:b>
      : the CMLLite schema defines the allowed attributes on elements, and their allowed children, however it can not perform further validation such
      as;
      <h:ul>
                  <h:li>the eldest cml:cml element MUST have version and convention specified</h:li>
                  <h:li>the ids of all the atoms referenced in a bond (via atomRefs2) must be defined within that molecule</h:li>
               </h:ul>
            </h:p>
            <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
                 xmlns:h="http://www.w3.org/1999/xhtml"
                 xmlns:dc="http://purl.org/dc/elements/1.1/"
                 xmlns:fn="http://www.w3.org/2005/02/xpath-functions">
      This stylesheet does not attempt to perform any chemical validation (other than that which is absolutely necessary for the document to make
      consistent sense - such as the rule about atom ids above) this is performed later.
    </h:p>
         </svrl:text>
         <svrl:ns-prefix-in-attribute-values uri="http://www.xml-cml.org/schema" prefix="cml"/>
         <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org//1999/XSL/Transform" prefix="xsl"/>
         <svrl:active-pattern>
            <xsl:attribute name="id">doc.checks</xsl:attribute>
            <xsl:attribute name="name">check for CMLLite</xsl:attribute>
            <svrl:title>check for CMLLite</svrl:title>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M10"/>
         <svrl:active-pattern>
            <xsl:attribute name="id">cml.checks</xsl:attribute>
            <xsl:attribute name="name">CML element checks</xsl:attribute>
            <svrl:title>CML element checks</svrl:title>
            <svrl:text>
               <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
                    xmlns:h="http://www.w3.org/1999/xhtml"
                    xmlns:dc="http://purl.org/dc/elements/1.1/"
                    xmlns:fn="http://www.w3.org/2005/02/xpath-functions">Describe what further limitations we have put on the cml element</h:p>
            </svrl:text>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M11"/>
         <svrl:active-pattern>
            <xsl:attribute name="id">scalar.checks</xsl:attribute>
            <xsl:attribute name="name">scalar element checks</xsl:attribute>
            <svrl:title>scalar element checks</svrl:title>
            <svrl:text>
               <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
                    xmlns:h="http://www.w3.org/1999/xhtml"
                    xmlns:dc="http://purl.org/dc/elements/1.1/"
                    xmlns:fn="http://www.w3.org/2005/02/xpath-functions">Describe what further limitations we have put on the cml element</h:p>
            </svrl:text>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M12"/>
         <svrl:active-pattern>
            <xsl:attribute name="id">label.checks</xsl:attribute>
            <xsl:attribute name="name">label element checks</xsl:attribute>
            <svrl:title>label element checks</svrl:title>
            <svrl:text>
               <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
                    xmlns:h="http://www.w3.org/1999/xhtml"
                    xmlns:dc="http://purl.org/dc/elements/1.1/"
                    xmlns:fn="http://www.w3.org/2005/02/xpath-functions">labels should have convention specified if at all possible</h:p>
            </svrl:text>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M13"/>
         <svrl:active-pattern>
            <xsl:attribute name="id">molecule.checks</xsl:attribute>
            <xsl:attribute name="name">molecule element checks</xsl:attribute>
            <svrl:title>molecule element checks</svrl:title>
            <svrl:text>
               <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
                    xmlns:h="http://www.w3.org/1999/xhtml"
                    xmlns:dc="http://purl.org/dc/elements/1.1/"
                    xmlns:fn="http://www.w3.org/2005/02/xpath-functions">
                  <div>how unique should the ids of molecule be?</div>
               </h:p>
            </svrl:text>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M14"/>
         <svrl:active-pattern>
            <xsl:attribute name="id">formula.checks</xsl:attribute>
            <xsl:attribute name="name">formula element checks</xsl:attribute>
            <svrl:title>formula element checks</svrl:title>
            <svrl:text>
               <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
                    xmlns:h="http://www.w3.org/1999/xhtml"
                    xmlns:dc="http://purl.org/dc/elements/1.1/"
                    xmlns:fn="http://www.w3.org/2005/02/xpath-functions">Describe what further limitations we have put on the atom element</h:p>
            </svrl:text>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M15"/>
         <svrl:active-pattern>
            <xsl:attribute name="id">peak.checks</xsl:attribute>
            <xsl:attribute name="name">peak element checks</xsl:attribute>
            <svrl:title>peak element checks</svrl:title>
            <svrl:text>
               <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
                    xmlns:h="http://www.w3.org/1999/xhtml"
                    xmlns:dc="http://purl.org/dc/elements/1.1/"
                    xmlns:fn="http://www.w3.org/2005/02/xpath-functions"/>
            </svrl:text>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M16"/>
         <svrl:active-pattern>
            <xsl:attribute name="id">peakList.checks</xsl:attribute>
            <xsl:attribute name="name">peakList element checks</xsl:attribute>
            <svrl:title>peakList element checks</svrl:title>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M17"/>
         <svrl:active-pattern>
            <xsl:attribute name="id">atom.checks</xsl:attribute>
            <xsl:attribute name="name">atom element checks</xsl:attribute>
            <svrl:title>atom element checks</svrl:title>
            <svrl:text>
               <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
                    xmlns:h="http://www.w3.org/1999/xhtml"
                    xmlns:dc="http://purl.org/dc/elements/1.1/"
                    xmlns:fn="http://www.w3.org/2005/02/xpath-functions">Describe what further limitations we have put on the atom element</h:p>
            </svrl:text>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M18"/>
         <svrl:active-pattern>
            <xsl:attribute name="id">bond.checks</xsl:attribute>
            <xsl:attribute name="name">bond element checks</xsl:attribute>
            <svrl:title>bond element checks</svrl:title>
            <svrl:text>
               <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
                    xmlns:h="http://www.w3.org/1999/xhtml"
                    xmlns:dc="http://purl.org/dc/elements/1.1/"
                    xmlns:fn="http://www.w3.org/2005/02/xpath-functions">Describe what further limitations we have put on the atom element</h:p>
            </svrl:text>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M19"/>
         <svrl:active-pattern>
            <xsl:attribute name="id">bondStereo.checks</xsl:attribute>
            <xsl:attribute name="name">bondStereo element checks</xsl:attribute>
            <svrl:title>bondStereo element checks</svrl:title>
            <svrl:text>
               <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
                    xmlns:h="http://www.w3.org/1999/xhtml"
                    xmlns:dc="http://purl.org/dc/elements/1.1/"
                    xmlns:fn="http://www.w3.org/2005/02/xpath-functions">Describe what further limitations we have put on the bondStereo element</h:p>
            </svrl:text>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M20"/>
         <svrl:active-pattern>
            <xsl:attribute name="id">atomArray.checks</xsl:attribute>
            <xsl:attribute name="name">atomArray element checks</xsl:attribute>
            <svrl:title>atomArray element checks</svrl:title>
            <svrl:text>
               <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
                    xmlns:h="http://www.w3.org/1999/xhtml"
                    xmlns:dc="http://purl.org/dc/elements/1.1/"
                    xmlns:fn="http://www.w3.org/2005/02/xpath-functions">
                  <h:div>
        atomArray must be in either molecule or formula but could be enclosed in a cml element (perhaps for some bizarre grouping)
      </h:div>
               </h:p>
            </svrl:text>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M21"/>
      </svrl:schematron-output>
   </xsl:template>

   <!--SCHEMATRON PATTERNS-->
<dc:title xmlns="http://purl.oclc.org/dsdl/schematron"
             xmlns:h="http://www.w3.org/1999/xhtml"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:fn="http://www.w3.org/2005/02/xpath-functions"/>
   <dc:author xmlns="http://purl.oclc.org/dsdl/schematron"
              xmlns:h="http://www.w3.org/1999/xhtml"
              xmlns:dc="http://purl.org/dc/elements/1.1/"
              xmlns:fn="http://www.w3.org/2005/02/xpath-functions"/>
   <dc:contributor xmlns="http://purl.oclc.org/dsdl/schematron"
                   xmlns:h="http://www.w3.org/1999/xhtml"
                   xmlns:dc="http://purl.org/dc/elements/1.1/"
                   xmlns:fn="http://www.w3.org/2005/02/xpath-functions"/>
   <dc:contributor xmlns="http://purl.oclc.org/dsdl/schematron"
                   xmlns:h="http://www.w3.org/1999/xhtml"
                   xmlns:dc="http://purl.org/dc/elements/1.1/"
                   xmlns:fn="http://www.w3.org/2005/02/xpath-functions"/>
   <dc:rights xmlns="http://purl.oclc.org/dsdl/schematron"
              xmlns:h="http://www.w3.org/1999/xhtml"
              xmlns:dc="http://purl.org/dc/elements/1.1/"
              xmlns:fn="http://www.w3.org/2005/02/xpath-functions"/>
   <dc:description xmlns="http://purl.oclc.org/dsdl/schematron"
                   xmlns:h="http://www.w3.org/1999/xhtml"
                   xmlns:dc="http://purl.org/dc/elements/1.1/"
                   xmlns:fn="http://www.w3.org/2005/02/xpath-functions"/>
   <svrl:title xmlns:svrl="http://purl.oclc.org/dsdl/svrl">CMLLite schematron file</svrl:title>

   <!--PATTERN doc.checkscheck for CMLLite-->
<svrl:title xmlns:svrl="http://purl.oclc.org/dsdl/svrl">check for CMLLite</svrl:title>

	  <!--RULE -->
<xsl:template match="/*" priority="3999" mode="M10">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="/*"/>

		    <!--REPORT -->
<xsl:if test="true()">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="true()">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-get-full-path"/>
            </xsl:attribute>
            <svrl:text>
        Report date:
        <xsl:text/>
               <xsl:value-of select="current-dateTime()"/>
               <xsl:text/>
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="//cml:cml"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="//cml:cml">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>the document does not contain any valid CMLLite</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()" mode="M10"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M10"/>
   <xsl:template match="@*|node()" priority="-2" mode="M10">
      <xsl:apply-templates select="@*|node()" mode="M10"/>
   </xsl:template>

   <!--PATTERN cml.checksCML element checks-->
<svrl:title xmlns:svrl="http://purl.oclc.org/dsdl/svrl">CML element checks</svrl:title>

	  <!--RULE -->
<xsl:template match="cml:cml" priority="3998" mode="M11">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="cml:cml"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="@convention or ancestor::cml:cml"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="@convention or ancestor::cml:cml">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>the eldest cml element must have @convention</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="@version or ancestor::cml:cml"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@version or ancestor::cml:cml">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>the eldest cml element must have @version</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="(@convention = 'CMLLite') or ancestor::cml:cml"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="(@convention = 'CMLLite') or ancestor::cml:cml">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>'CMLLite' expected as @convention on the eldest cml element</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()" mode="M11"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M11"/>
   <xsl:template match="@*|node()" priority="-2" mode="M11">
      <xsl:apply-templates select="@*|node()" mode="M11"/>
   </xsl:template>

   <!--PATTERN scalar.checksscalar element checks-->
<svrl:title xmlns:svrl="http://purl.oclc.org/dsdl/svrl">scalar element checks</svrl:title>

	  <!--RULE -->
<xsl:template match="cml:scalar" priority="3998" mode="M12">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="cml:scalar"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="count(@max | @min | text()) &gt;= 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(@max | @min | text()) &gt;= 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>scalar must have one or more max, min or content</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()" mode="M12"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M12"/>
   <xsl:template match="@*|node()" priority="-2" mode="M12">
      <xsl:apply-templates select="@*|node()" mode="M12"/>
   </xsl:template>

   <!--PATTERN label.checkslabel element checks-->
<svrl:title xmlns:svrl="http://purl.oclc.org/dsdl/svrl">label element checks</svrl:title>

	  <!--RULE -->
<xsl:template match="cml:label" priority="3998" mode="M13">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="cml:label"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not(@convention)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="not(@convention)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>label should have convention specified if at all possible</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()" mode="M13"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M13"/>
   <xsl:template match="@*|node()" priority="-2" mode="M13">
      <xsl:apply-templates select="@*|node()" mode="M13"/>
   </xsl:template>

   <!--PATTERN molecule.checksmolecule element checks-->
<svrl:title xmlns:svrl="http://purl.oclc.org/dsdl/svrl">molecule element checks</svrl:title>

	  <!--RULE -->
<xsl:template match="cml:molecule" priority="3998" mode="M14">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="cml:molecule"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="@count or not(ancestor::cml:molecule)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="@count or not(ancestor::cml:molecule)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>molecule children of molecule require a count</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not(@count) or (floor(@count) = number(@count))"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="not(@count) or (floor(@count) = number(@count))">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>@count must be integer</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()" mode="M14"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M14"/>
   <xsl:template match="@*|node()" priority="-2" mode="M14">
      <xsl:apply-templates select="@*|node()" mode="M14"/>
   </xsl:template>

   <!--PATTERN formula.checksformula element checks-->
<svrl:title xmlns:svrl="http://purl.oclc.org/dsdl/svrl">formula element checks</svrl:title>

	  <!--RULE -->
<xsl:template match="cml:formula" priority="3998" mode="M15">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="cml:formula"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="@count or not(ancestor::cml:formula)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="@count or not(ancestor::cml:formula)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>formula children of formula require a count</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not(@count) or (floor(@count) = number(@count))"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="not(@count) or (floor(@count) = number(@count))">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>@count must be integer</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="not(@concise)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="not(@concise)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-get-full-path"/>
            </xsl:attribute>
            <svrl:text>a formula should have @concise if at all possible</svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()" mode="M15"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M15"/>
   <xsl:template match="@*|node()" priority="-2" mode="M15">
      <xsl:apply-templates select="@*|node()" mode="M15"/>
   </xsl:template>

   <!--PATTERN peak.checkspeak element checks-->
<svrl:title xmlns:svrl="http://purl.oclc.org/dsdl/svrl">peak element checks</svrl:title>

	  <!--RULE -->
<xsl:template match="cml:peak" priority="3998" mode="M16">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="cml:peak"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="@yValue and count(ancestor::cml:peakList[1]//cml:peak/@yValue) = count(ancestor::cml:peakList[1]//cml:peak)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="@yValue and count(ancestor::cml:peakList[1]//cml:peak/@yValue) = count(ancestor::cml:peakList[1]//cml:peak)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>if peak has yValue then all peaks should have yValue</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="@xValue or (xMax and xMin)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@xValue or (xMax and xMin)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>the peak must have xValue and/or (xMax and xMin)</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not(@xMax) or (@xMax and @xMin)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="not(@xMax) or (@xMax and @xMin)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>peak must not have an isolated xMax attribute</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not(@xMin) or (@xMax and @xMin)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="not(@xMin) or (@xMax and @xMin)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>peak must not have an isolated xMin attribute</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()" mode="M16"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M16"/>
   <xsl:template match="@*|node()" priority="-2" mode="M16">
      <xsl:apply-templates select="@*|node()" mode="M16"/>
   </xsl:template>

   <!--PATTERN peakList.checkspeakList element checks-->
<svrl:title xmlns:svrl="http://purl.oclc.org/dsdl/svrl">peakList element checks</svrl:title>

	  <!--RULE -->
<xsl:template match="cml:peakList" priority="3999" mode="M17">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="cml:peakList"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="count(.//cml:peak) &gt;= 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(.//cml:peak) &gt;= 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>peakList must contain at least one peak element</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="count(.//cml:peak[@yValue]) &gt; 0 and @yUnits"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(.//cml:peak[@yValue]) &gt; 0 and @yUnits">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>if peaks have y values then peakList must specify yUnits</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()" mode="M17"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M17"/>
   <xsl:template match="@*|node()" priority="-2" mode="M17">
      <xsl:apply-templates select="@*|node()" mode="M17"/>
   </xsl:template>

   <!--PATTERN atom.checksatom element checks-->
<svrl:title xmlns:svrl="http://purl.oclc.org/dsdl/svrl">atom element checks</svrl:title>

	  <!--RULE -->
<xsl:template match="cml:atom" priority="3998" mode="M18">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="cml:atom"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="count(ancestor::cml:molecule[1]//cml:atom[@id = current()/@id]) = 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(ancestor::cml:molecule[1]//cml:atom[@id = current()/@id]) = 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>the id of a atom must be unique within the eldest containing molecule (duplicate found: <xsl:text/>
                  <xsl:value-of select="@id"/>
                  <xsl:text/>)</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not(@x2) or (@x2 and @y2)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="not(@x2) or (@x2 and @y2)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>if atom has @x2 then it must have @y2</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not(@y2) or (@x2 and @y2)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="not(@y2) or (@x2 and @y2)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>if atom has @y2 then it must have @x2</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not(@x3) or (@x3 and @y3 and @z3)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="not(@x3) or (@x3 and @y3 and @z3)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>if atom has @x3 then it must have @y3 and @z3</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not(@y3) or (@x3 and @y3 and @z3)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="not(@y3) or (@x3 and @y3 and @z3)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>if atom has @32 then it must have @x3 and @z3</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not(@z3) or (@x3 and @y3 and @z3)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="not(@z3) or (@x3 and @y3 and @z3)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>if atom has @z3 then it must have @x3 and @y3</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()" mode="M18"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M18"/>
   <xsl:template match="@*|node()" priority="-2" mode="M18">
      <xsl:apply-templates select="@*|node()" mode="M18"/>
   </xsl:template>

   <!--PATTERN bond.checksbond element checks-->
<svrl:title xmlns:svrl="http://purl.oclc.org/dsdl/svrl">bond element checks</svrl:title>

	  <!--RULE -->
<xsl:template match="cml:bond" priority="3998" mode="M19">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="cml:bond"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="index-of(ancestor::cml:molecule[1]//cml:atom/@id, substring-before(@atomRefs2, ' ')) &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="index-of(ancestor::cml:molecule[1]//cml:atom/@id, substring-before(@atomRefs2, ' ')) &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>the atoms in the atomRefs2 must be within the eldest containing molecule (found <xsl:text/>
                  <xsl:value-of select="substring-before(@atomRefs2, ' ')"/>
                  <xsl:text/>)</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="index-of(ancestor::cml:molecule[1]//cml:atom/@id, substring-after(@atomRefs2, ' ')) &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="index-of(ancestor::cml:molecule[1]//cml:atom/@id, substring-after(@atomRefs2, ' ')) &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>the atoms in the atomRefs2 must be within the eldest containing molecule (found <xsl:text/>
                  <xsl:value-of select="substring-after(@atomRefs2, ' ')"/>
                  <xsl:text/>)</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not(substring-before(@atomRefs2, ' ') = substring-after(@atomRefs2, ' '))"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="not(substring-before(@atomRefs2, ' ') = substring-after(@atomRefs2, ' '))">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>a bond must be between different atoms</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="count(ancestor::cml:molecule[1]//cml:bond[@id = current()/@id]) = 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(ancestor::cml:molecule[1]//cml:bond[@id = current()/@id]) = 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>the id of a bond must be unique within the eldest containing molecule (duplicate found: <xsl:text/>
                  <xsl:value-of select="@id"/>
                  <xsl:text/>)</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()" mode="M19"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M19"/>
   <xsl:template match="@*|node()" priority="-2" mode="M19">
      <xsl:apply-templates select="@*|node()" mode="M19"/>
   </xsl:template>

   <!--PATTERN bondStereo.checksbondStereo element checks-->
<svrl:title xmlns:svrl="http://purl.oclc.org/dsdl/svrl">bondStereo element checks</svrl:title>

	  <!--RULE -->
<xsl:template match="cml:bondStereo" priority="3998" mode="M20">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="cml:bondStereo"/>
      <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
           xmlns:h="http://www.w3.org/1999/xhtml"
           xmlns:dc="http://purl.org/dc/elements/1.1/"
           xmlns:fn="http://www.w3.org/2005/02/xpath-functions">
         <h:div/>
      </h:p>

		    <!--REPORT -->
<xsl:if test="not(@convention='cml:wedgehatch') or not(@convention='cml:cistrans')">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                 test="not(@convention='cml:wedgehatch') or not(@convention='cml:cistrans')">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-get-full-path"/>
            </xsl:attribute>
            <svrl:text>only cml:wedgehatch and cml:cistrans bondStereo are currently supported</svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="@convention='cml:wedgehatch' and not(@atomRefs4)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="@convention='cml:wedgehatch' and not(@atomRefs4)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>atomRefs4 should not be present for wedge/hatch bondStereo</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="@convention='cml:cistrans' and @atomRefs4"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="@convention='cml:cistrans' and @atomRefs4">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>atomRefs4 are required for cis/trans bondStereo (to define what is cis or trans to what)</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
           xmlns:h="http://www.w3.org/1999/xhtml"
           xmlns:dc="http://purl.org/dc/elements/1.1/"
           xmlns:fn="http://www.w3.org/2005/02/xpath-functions">
         <h:div/>
      </h:p>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="(@convention='cml:wedgehatch' and . = 'W') or (@convention='cml:wedgehatch' and . = 'H')"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="(@convention='cml:wedgehatch' and . = 'W') or (@convention='cml:wedgehatch' and . = 'H')">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>
        if the convention is cml:wedgehatch then the content should be either W or H
      </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
           xmlns:h="http://www.w3.org/1999/xhtml"
           xmlns:dc="http://purl.org/dc/elements/1.1/"
           xmlns:fn="http://www.w3.org/2005/02/xpath-functions">
         <h:div/>
      </h:p>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="(@convention='cml:cistrans' and . = 'C') or (@convention='cml:cistrans' and . = 'T')"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="(@convention='cml:cistrans' and . = 'C') or (@convention='cml:cistrans' and . = 'T')">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>
        if the convention is cml:cistrans then the content should be either C or T
      </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()" mode="M20"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M20"/>
   <xsl:template match="@*|node()" priority="-2" mode="M20">
      <xsl:apply-templates select="@*|node()" mode="M20"/>
   </xsl:template>

   <!--PATTERN atomArray.checksatomArray element checks-->
<svrl:title xmlns:svrl="http://purl.oclc.org/dsdl/svrl">atomArray element checks</svrl:title>

	  <!--RULE -->
<xsl:template match="cml:atomArray" priority="3998" mode="M21">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="cml:atomArray"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="ancestor::cml:molecule or ancestor::cml:formula"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="ancestor::cml:molecule or ancestor::cml:formula">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>atomArray must be found in either a molecule or a formula</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <h:p xmlns="http://purl.oclc.org/dsdl/schematron"
           xmlns:h="http://www.w3.org/1999/xhtml"
           xmlns:dc="http://purl.org/dc/elements/1.1/"
           xmlns:fn="http://www.w3.org/2005/02/xpath-functions"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test=".//cml:atom"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test=".//cml:atom">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>an atomArray must contain atoms</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()" mode="M21"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M21"/>
   <xsl:template match="@*|node()" priority="-2" mode="M21">
      <xsl:apply-templates select="@*|node()" mode="M21"/>
   </xsl:template>
</xsl:stylesheet>