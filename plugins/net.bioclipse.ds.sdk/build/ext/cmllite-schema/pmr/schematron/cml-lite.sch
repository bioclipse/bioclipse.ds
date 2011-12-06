<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" xmlns:cml="http://www.xml-cml.org/schema"
  xmlns:h="http://www.w3.org/1999/xhtml" xmlns:dc="http://purl.org/dc/elements/1.1/" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
  queryBinding='xslt2' version="2.0" schemaVersion="ISO19757-3">
  <dc:title>CMLLite schematron file</dc:title>
  <dc:author>J. A. Townsend, jat45@cantab.net</dc:author>
  <dc:contributor>Peter Murray-Rust pm286@cam.ac.uk</dc:contributor>
  <dc:contributor>Jim Downing ojd21@cam.ac.uk</dc:contributor>
  <dc:rights>&#164; Copyright J. A. Townsend jat45@cantab.net 2008</dc:rights>
  <dc:description>Checks that the elements and attributes being used conform to the structure and subset as agreed upon by MS and UCC.</dc:description>
  <title>CMLLite schematron file</title>
  <p>
    <h:p>Checks that the elements and attributes being used conform to the structure and subset as agreed upon by MS and UCC.</h:p>
    <h:p>
      <h:b>NOTE</h:b>
      : the CMLLite schema defines the allowed attributes on elements, and their allowed children, however it can not perform further validation such
      as;
      <h:ul>
        <h:li>the eldest cml:cml element MUST have version and convention specified</h:li>
        <h:li>the ids of all the atoms referenced in a bond (via atomRefs2) must be defined within that molecule</h:li>
      </h:ul>
    </h:p>
    <h:p>
      This stylesheet does not attempt to perform any chemical validation (other than that which is absolutely necessary for the document to make
      consistent sense - such as the rule about atom ids above) this is performed later.
    </h:p>
  </p>

  <ns prefix="cml" uri="http://www.xml-cml.org/schema" />
  <ns prefix="xsl" uri="http://www.w3.org//1999/XSL/Transform" />

     
  <pattern id="doc.checks">
    <title>check for CMLLite</title>
    <rule context="/*">
      <report test="true()">
        Report date:
        <value-of select="current-dateTime()" />
      </report>
      <assert test="//cml:cml">the document does not contain any valid CMLLite</assert>
    </rule>
  </pattern>

  <pattern id="cml.checks">
    <title>CML element checks</title>
    <p><h:p>Describe what further limitations we have put on the cml element</h:p></p>
    <rule context="cml:cml">
      <assert test="@convention or ancestor::cml:cml">the eldest cml element must have @convention</assert>
      <assert test="@version or ancestor::cml:cml">the eldest cml element must have @version</assert>
      <p>
      This schematron is designed to validate CMLLite, therefore if the convention is not CMLLite we should be worried - however,
      it is possible that we want to validate more than just CMLLite (many of the restrictions placed on ordering etc make it 
      easier to use this form of CML rather than the more general form) maybe we can introduce a flag to turn on CMLLite validation
      or looser validation
      </p>
      <assert test="(@convention = 'CMLLite') or ancestor::cml:cml">'CMLLite' expected as @convention on the eldest cml element</assert>
    </rule>
  </pattern>

  <pattern id="scalar.checks">
    <title>scalar element checks</title>
    <p><h:p>Describe what further limitations we have put on the cml element</h:p></p>
    <rule context="cml:scalar">
      <assert test="count(@max | @min | text()) >= 1">scalar must have one or more max, min or content</assert>
    </rule>
  </pattern>

  <pattern id="label.checks">
    <title>label element checks</title>
    <p><h:p>labels should have convention specified if at all possible</h:p></p>
    <rule context="cml:label">
      <assert test="not(@convention)">label should have convention specified if at all possible</assert>
    </rule>
  </pattern>

  
  <pattern id="molecule.checks">
    <title>molecule element checks</title>
    <p><h:p><div class="question">how unique should the ids of molecule be?</div></h:p></p>
    <rule context="cml:molecule">
      <assert test="@count or not(ancestor::cml:molecule)">molecule children of molecule require a count</assert>
      <assert test="not(@count) or (floor(@count) = number(@count))">@count must be integer</assert>
    </rule>    
  </pattern>

  <pattern id="formula.checks">
    <title>formula element checks</title>
    <p><h:p>Describe what further limitations we have put on the atom element</h:p></p>
    <rule context="cml:formula">
      <assert test="@count or not(ancestor::cml:formula)">formula children of formula require a count</assert>
      <assert test="not(@count) or (floor(@count) = number(@count))">@count must be integer</assert>
      <report test="not(@concise)">a formula should have @concise if at all possible</report>
    </rule>    
  </pattern>

  <pattern id="peak.checks">
    <title>peak element checks</title>
    <p><h:p></h:p></p>
    <rule context="cml:peak">
      <p>
        if peak has yValue then all peaks in this peakList should have yValue
      </p>
      <assert test="@yValue and count(ancestor::cml:peakList[1]//cml:peak/@yValue) = count(ancestor::cml:peakList[1]//cml:peak)">if peak has yValue then all peaks should have yValue</assert>
      <p>
        <h:p>
        A peak must have xMax if xMin is specified and visa versa, it must also always have a value 
        - whether this is specified using xMax and xMin just by xValue.
        </h:p>
      </p>    
      <assert test="@xValue or (xMax and xMin)">the peak must have xValue and/or (xMax and xMin)</assert>
      <assert test="not(@xMax) or (@xMax and @xMin)">peak must not have an isolated xMax attribute</assert>
      <assert test="not(@xMin) or (@xMax and @xMin)">peak must not have an isolated xMin attribute</assert> 
    </rule>    
  </pattern>

  <pattern id="peakList.checks">
    <title>peakList element checks</title>
    <rule context="cml:peakList">
    <p><h:p>peakList must containt at least one peak</h:p></p>
      <assert test="count(.//cml:peak) >= 1">peakList must contain at least one peak element</assert>
    <p><h:p>peakList must have yUnits specified if any of the peaks have yValue</h:p></p>
      <assert test="count(.//cml:peak[@yValue]) > 0 and @yUnits">if peaks have y values then peakList must specify yUnits</assert>
    </rule>    
  </pattern>

  
  <pattern id="atom.checks">
    <title>atom element checks</title>
    <p><h:p>Describe what further limitations we have put on the atom element</h:p></p>
    <rule context="cml:atom">
      <p>
      Check that the id of this atom is unique within the eldest containing molecule
      The schema validation specifies that each atom must have an id, this check tests
      the uniqueness
      </p>
      <assert test="count(ancestor::cml:molecule[1]//cml:atom[@id = current()/@id]) = 1">the id of a atom must be unique within the eldest containing molecule (duplicate found: <value-of select="@id" />)</assert>
      <p>
      If x2 is present the y2 must be present
      </p>
      <assert test="not(@x2) or (@x2 and @y2)">if atom has @x2 then it must have @y2</assert>
      <p>
      If y2 is present the x2 must be present
      </p>
      <assert test="not(@y2) or (@x2 and @y2)">if atom has @y2 then it must have @x2</assert>
      <p>
      If x3 is present then y3 and z3 must be present
      </p>
      <assert test="not(@x3) or (@x3 and @y3 and @z3)">if atom has @x3 then it must have @y3 and @z3</assert>
      <p>
      If y3 is present then x3 and z3 must be present
      </p>
      <assert test="not(@y3) or (@x3 and @y3 and @z3)">if atom has @32 then it must have @x3 and @z3</assert>
      <p>
      If x3 is present then y3 and z3 must be present
      </p>
      <assert test="not(@z3) or (@x3 and @y3 and @z3)">if atom has @z3 then it must have @x3 and @y3</assert>
    </rule>    
  </pattern>

  <pattern id="bond.checks">
    <title>bond element checks</title>
    <p><h:p>Describe what further limitations we have put on the atom element</h:p></p>
    <rule context="cml:bond">
      <p>
        Check that the first atom in the atomRefs2 attribute exists within the same molecule        
      </p>
      <assert test="index-of(ancestor::cml:molecule[1]//cml:atom/@id, substring-before(@atomRefs2, ' ')) > 0">the atoms in the atomRefs2 must be within the eldest containing molecule (found <value-of select="substring-before(@atomRefs2, ' ')" />)</assert>
      <p>
        Check that the second atom in the atomRefs2 attribute exists within the same molecule        
      </p>
      <assert test="index-of(ancestor::cml:molecule[1]//cml:atom/@id, substring-after(@atomRefs2, ' ')) > 0">the atoms in the atomRefs2 must be within the eldest containing molecule (found <value-of select="substring-after(@atomRefs2, ' ')" />)</assert>
      <p>
        Check that the first atom and second atom in atomRefs2 are not the same
      </p>
      <assert test="not(substring-before(@atomRefs2, ' ') = substring-after(@atomRefs2, ' '))">a bond must be between different atoms</assert>
      <p>
      Check that the id of this bond is unique within the eldest containing molecule
      The schema validation specifies that each bond must have an id, this check tests
      the uniqueness
      </p>
      <assert test="count(ancestor::cml:molecule[1]//cml:bond[@id = current()/@id]) = 1">the id of a bond must be unique within the eldest containing molecule (duplicate found: <value-of select="@id" />)</assert>
    </rule>    
  </pattern>

  <pattern id="bondStereo.checks">
    <title>bondStereo element checks</title>
    <p><h:p>Describe what further limitations we have put on the bondStereo element</h:p></p>
    <rule context="cml:bondStereo">
      <h:p>
        CMLLite only supports wedge/hatch and cis/trans bonds but CML allows 
        for any convention to be used <h:div class='question'>can we have a flag?</h:div>        
      </h:p>
      <report test="not(@convention='cml:wedgehatch') or not(@convention='cml:cistrans')">only cml:wedgehatch and cml:cistrans bondStereo are currently supported</report>
      <assert test="@convention='cml:wedgehatch' and not(@atomRefs4)">atomRefs4 should not be present for wedge/hatch bondStereo</assert>
      <assert test="@convention='cml:cistrans' and @atomRefs4">atomRefs4 are required for cis/trans bondStereo (to define what is cis or trans to what)</assert>
      <h:p>
        If the convention is cml:wedgehatch then the content should be either W or H
        <h:div class="question">should we normalise space ??</h:div>
      </h:p>
      <assert test="(@convention='cml:wedgehatch' and . = 'W') or (@convention='cml:wedgehatch' and . = 'H')">
        if the convention is cml:wedgehatch then the content should be either W or H
      </assert>
      <h:p>
        If the convention is cml:cistrans then the content should be either C or T
        <h:div class="question">should we normalise space ??</h:div>
      </h:p>
      <assert test="(@convention='cml:cistrans' and . = 'C') or (@convention='cml:cistrans' and . = 'T')">
        if the convention is cml:cistrans then the content should be either C or T
      </assert>
    </rule>
  </pattern>

  <pattern id="atomArray.checks">
    <title>atomArray element checks</title>
    <p>
    <h:p>
      <h:div class="question">
        atomArray must be in either molecule or formula but could be enclosed in a cml element (perhaps for some bizarre grouping)
      </h:div>
    </h:p>
    </p>
    <rule context="cml:atomArray">
      <assert test="ancestor::cml:molecule or ancestor::cml:formula">atomArray must be found in either a molecule or a formula</assert>
      <h:p>an atomArray must contain atoms</h:p>
      <assert test=".//cml:atom">an atomArray must contain atoms</assert>
    </rule>    
  </pattern>

 

</schema>

