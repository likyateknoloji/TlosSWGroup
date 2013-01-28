
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:par="http://www.likyateknoloji.com/XML_parameters_types" xmlns:dat="http://www.likyateknoloji.com/XML_data_types"
                xmlns:com="http://www.likyateknoloji.com/XML_common_types" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:functx="http://www.functx.com">
	<xsl:output method="xml"/>

	<xsl:variable name="sonucDeger" as="xs:string?"/>

	<xsl:template match="*|@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template name="dots">

		<xsl:param name="numberOfParameter" select="1"/>
		<xsl:param name="paramValue" select="X"/>
		<xsl:param name="nodeValue" select="Y"/>

		<xsl:if test="$numberOfParameter &gt; 0">

			<!--			<xsl:text>&#xA; NumberOfParameters </xsl:text>
			<xsl:value-of select="$numberOfParameter"/>
			<xsl:text>&#xA; Parametre Bilgisi </xsl:text>
			<xsl:sequence select="$paramValue[$numberOfParameter]"/>
			<xsl:text>&#xA; Node Value </xsl:text>
			<xsl:value-of select="$nodeValue"/>-->

			<!--<xsl:text>&#xA; Parametre degeri ;   </xsl:text>-->
			<xsl:variable name="param" select="concat('$(',$paramValue[$numberOfParameter]/par:name/text(),')')"/>
			<xsl:variable name="paramEscaped" select="$param"/>
			<!--<xsl:value-of select="$param"/>-->
			<!--<xsl:text> = </xsl:text>-->
			<xsl:variable name="value" select="$paramValue[$numberOfParameter]/par:valueString/text()"/>

			<!--<xsl:value-of select="concat('''', $value, '''')"/>-->

			<xsl:variable name="par" select="$nodeValue"/>

			<!--<xsl:text>&#xA; SONUC = </xsl:text>-->

			<xsl:variable name="modifiedNode">
				<xsl:choose>
					<xsl:when test="contains($par , $param )">
						<!--ss><xsl:value-of select="translate($par , $param, concat($value,'$1'))"/></ss-->
						<!--replace(string, regex, regex-replace)-->

						<xsl:variable name="sonucDeger">
							<xsl:call-template name="replace-substring">
								<xsl:with-param name="original" select="$par"/>
								<xsl:with-param name="substring" select="$param"/>
								<xsl:with-param name="replacement" select="$value"/>
							</xsl:call-template>
						</xsl:variable>
						<!--xsl:variable name="sonucDeger" select="replace($par , $paramEscaped, functx:escape-for-regex($value))"/-->
						<xsl:value-of select="$sonucDeger"/>
					</xsl:when>
					<xsl:otherwise>

						<xsl:variable name="sonucDeger" select="$par"/>
						<xsl:value-of select="$sonucDeger"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<!--<xsl:text>&#xA;</xsl:text>
			<xsl:text>&#xA; Sonuc = </xsl:text>
			<xsl:value-of select="$modifiedNode"/>-->


			<xsl:call-template name="dots">
				<xsl:with-param name="numberOfParameter" select="$numberOfParameter - 1"/>
				<xsl:with-param name="paramValue" select="$paramValue"/>
				<xsl:with-param name="nodeValue" select="$modifiedNode"/>
			</xsl:call-template>
		</xsl:if>
		<xsl:if test="$numberOfParameter = 0">
			<xsl:value-of select="$nodeValue/text()"/>
		</xsl:if>
	</xsl:template>


	<xsl:template match="com:jobPath|com:jobCommand">

		<xsl:comment>Created by using <xsl:value-of select="system-property('xsl:vendor')"/></xsl:comment>
		<xsl:variable name="par" select="current()"/>
		<!--<xsl:variable name="nn" select="concat('/<','/>')"/>-->
		<!--<xsl:text>&#xA; $par degerii = </xsl:text>-->
		<!--<xsl:value-of select="$par"/>-->

		<xsl:text disable-output-escaping="yes">&#60;</xsl:text>
		<xsl:value-of select="name(.)"/>
        <xsl:text disable-output-escaping="yes">&#62;</xsl:text>
		<!--<com:jobPath>-->
			<xsl:call-template name="dots">
				<xsl:with-param name="numberOfParameter" select="count(//par:parameter)"/>
				<xsl:with-param name="paramValue" select="//par:parameter"/>
				<xsl:with-param name="nodeValue" select="$par"/>
			</xsl:call-template>
		<!--</com:jobPath>-->

		<xsl:text disable-output-escaping="yes">&#60;/</xsl:text>
		<xsl:value-of select="name(.)"/>
        <xsl:text disable-output-escaping="yes">&#62;</xsl:text>
	</xsl:template>


	<xsl:template name="replace-substring">
		<xsl:param name="original"/>
		<xsl:param name="substring"/>
		<xsl:param name="replacement" select="''"/>

		<!--xsl:text>&#xA; Orjinal deger = </xsl:text-->
		<!--<xsl:value-of select="$original"/>-->
		<!--<xsl:text>&#xA;  </xsl:text>-->

		<!--<xsl:text>&#xA; substring deger = </xsl:text>-->
		<!--<xsl:value-of select="$substring"/>-->
		<!--<xsl:text>&#xA;  </xsl:text>-->

		<!--<xsl:text>&#xA; replacement deger = </xsl:text>-->
		<!--<xsl:value-of select="$replacement"/>-->
		<!--<xsl:text>&#xA;  </xsl:text>-->

		<xsl:variable name="first">
			<xsl:choose>
				<xsl:when test="contains($original, $substring)">
					<xsl:value-of select="substring-before($original, $substring)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$original"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="middle">
			<xsl:choose>
				<xsl:when test="contains($original, $substring)">
					<xsl:value-of select="$replacement"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text></xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="last">
			<xsl:choose>
				<xsl:when test="contains($original, $substring)">
					<xsl:choose>
						<xsl:when test="contains(substring-after($original, $substring), $substring)">
							<xsl:call-template name="replace-substring">
								<xsl:with-param name="original">
									<xsl:value-of select="substring-after($original, $substring)"/>
								</xsl:with-param>
								<xsl:with-param name="substring">
									<xsl:value-of select="$substring"/>
								</xsl:with-param>
								<xsl:with-param name="replacement">
									<xsl:value-of select="$replacement"/>
								</xsl:with-param>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="substring-after($original, $substring)"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text></xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:value-of select="concat($first, $middle, $last)"/>
	</xsl:template>
</xsl:stylesheet>