<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy® -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  <body>
  <h2>Result Summary</h2>
    <table border="1">
      <tr bgcolor="#9acd32">
 	<th>ID</th>
        <th>Input</th>
        <th>Veml</th>
		<th>cypher</th>
	    <th>filtered text</th>
        <th>stanford</th>
        <th>dependencyFiltered</th>
        <th>pos_tag</th> 		
		<th>rdf</th>
        <th>answers</th>
        <th>sparql</th>
        <th>Log</th>
      </tr>
      <xsl:for-each select="root/result">
      <tr>
   	 <td><xsl:value-of select="id"/></td>
        <td><xsl:value-of select="input"/></td>
        <td><pre><xsl:value-of select="veml"/></pre></td>
        <td><pre><xsl:value-of select="cypher_output"/></pre></td> 
		 <td><xsl:value-of select="textFiltered"/></td>
        <td><pre><xsl:value-of select="stanford"/></pre></td>
        <td><xsl:value-of select="dependencyFiltered"/></td>
        <td><pre><xsl:value-of select="stanford_tag"/></pre></td>
        <td><pre><xsl:value-of select="rdf_output"/></pre></td>
        <td><pre><xsl:value-of select="sparql_result"/></pre></td>
        <td><pre><xsl:value-of select="sparql_output"/></pre></td> 
        <td><pre><xsl:value-of select="error_log"/></pre></td>
      </tr>
      </xsl:for-each>
    </table>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>