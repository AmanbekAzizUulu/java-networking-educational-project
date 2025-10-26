<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:hm="http://example.com/http-methods"
                exclude-result-prefixes="hm">

    <xsl:output method="html" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/">
        <html lang="en">
            <head>
                <meta charset="UTF-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                <title>HTTP Methods Reference</title>
                <link rel="stylesheet" href="../properties/styles/styles.css"/>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🌐 HTTP Methods Reference</h1>
                        <p>Complete guide to REST API HTTP methods and their usage</p>
                    </div>

                    <div class="methods-grid">
                        <xsl:apply-templates select="hm:httpMethods/hm:method"/>
                    </div>
                </div>

                <script src="../properties/scripts/script.js"></script>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="hm:method">
        <div class="method-card">
            <div class="method-header method-{@name}">
                <div class="method-name">
                    <xsl:value-of select="@name"/>
                    <xsl:call-template name="get-method-badge">
                        <xsl:with-param name="method" select="@name"/>
                    </xsl:call-template>
                </div>
                <div class="method-description">
                    <xsl:value-of select="hm:description"/>
                </div>
            </div>

            <div class="method-content">
                <!-- Characteristics -->
                <div class="section">
                    <div class="section-title">📊 Characteristics</div>
                    <div class="characteristics">
                        <xsl:apply-templates select="hm:characteristics/hm:characteristic"/>
                    </div>
                </div>

                <!-- Request & Response -->
                <div class="section">
                    <div class="section-title">📨 Request &amp; Response</div>
                    <div class="request-response">
                        <div class="request">
                            <div class="sub-section">
                                <div class="sub-section-title">📤 Request</div>
                                <div class="sub-section">
                                    <strong>Body:</strong> <xsl:value-of select="hm:request/hm:body"/>
                                </div>
                                <div class="sub-section">
                                    <strong>Parameters:</strong> <xsl:value-of select="hm:request/hm:parameters"/>
                                </div>
                                <div class="sub-section">
                                    <strong>Headers:</strong>
                                    <ul class="headers-list">
                                        <xsl:for-each select="hm:request/hm:headers/hm:header">
                                            <li><xsl:value-of select="."/></li>
                                        </xsl:for-each>
                                    </ul>
                                </div>
                            </div>
                        </div>

                        <div class="response">
                            <div class="sub-section">
                                <div class="sub-section-title">📥 Response</div>
                                <div class="sub-section">
                                    <strong>Body:</strong> <xsl:value-of select="hm:response/hm:body"/>
                                </div>
                                <div class="sub-section">
                                    <strong>Status Codes:</strong>
                                    <ul class="status-codes">
                                        <xsl:for-each select="hm:response/hm:statusCodes/hm:code">
                                            <li>
                                                <span class="status-code status-{substring-before(., ' ')}">
                                                    <xsl:value-of select="."/>
                                                </span>
                                            </li>
                                        </xsl:for-each>
                                    </ul>
                                </div>
                                <xsl:if test="hm:response/hm:headers">
                                    <div class="sub-section">
                                        <strong>Headers:</strong>
                                        <ul class="headers-list">
                                            <xsl:for-each select="hm:response/hm:headers/hm:header">
                                                <li>
                                                    <xsl:value-of select="."/>
                                                    <xsl:if test="@name">
                                                        (<xsl:value-of select="@name"/>)
                                                    </xsl:if>
                                                </li>
                                            </xsl:for-each>
                                        </ul>
                                    </div>
                                </xsl:if>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Use Cases -->
                <div class="section">
                    <div class="section-title">💼 Use Cases</div>
                    <ul class="use-cases">
                        <xsl:for-each select="hm:useCases/hm:useCase">
                            <li><xsl:value-of select="."/></li>
                        </xsl:for-each>
                    </ul>
                </div>

                <!-- Example -->
                <div class="section">
                    <div class="section-title">🔍 Example</div>
                    <div class="example">
                        <div class="example-url">
                            <xsl:value-of select="hm:example/hm:url"/>
                        </div>

                        <xsl:if test="hm:example/hm:request">
                            <div class="example-request">
                                <div class="example-title">Request</div>
                                <xsl:if test="hm:example/hm:request/hm:contentType">
                                    <div style="margin-bottom: 10px;">
                                        <strong>Content-Type:</strong>
                                        <xsl:value-of select="hm:example/hm:request/hm:contentType"/>
                                    </div>
                                </xsl:if>
                                <div class="code-block">
                                    <xsl:value-of select="hm:example/hm:request/hm:body"/>
                                </div>
                            </div>
                        </xsl:if>

                        <div class="example-response">
                            <div class="example-title">Response</div>
                            <div style="margin-bottom: 10px;">
                                <strong>Status:</strong>
                                <span class="status-code status-{substring-before(hm:example/hm:response/hm:code, ' ')}">
                                    <xsl:value-of select="hm:example/hm:response/hm:code"/>
                                </span>
                            </div>
                            <xsl:if test="hm:example/hm:response/hm:contentType">
                                <div style="margin-bottom: 10px;">
                                    <strong>Content-Type:</strong>
                                    <xsl:value-of select="hm:example/hm:response/hm:contentType"/>
                                </div>
                            </xsl:if>
                            <xsl:if test="hm:example/hm:response/hm:headers">
                                <div style="margin-bottom: 10px;">
                                    <strong>Headers:</strong>
                                    <ul style="color: #bdc3c7; margin-left: 20px;">
                                        <xsl:for-each select="hm:example/hm:response/hm:headers/hm:header">
                                            <li>
                                                <xsl:value-of select="@name"/>: <xsl:value-of select="."/>
                                            </li>
                                        </xsl:for-each>
                                    </ul>
                                </div>
                            </xsl:if>
                            <div class="code-block">
                                <xsl:value-of select="hm:example/hm:response/hm:body"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </xsl:template>

    <xsl:template match="hm:characteristic">
        <xsl:variable name="char-class">
            <xsl:choose>
                <xsl:when test="contains(., 'Safe')">characteristic-<xsl:value-of select="translate(., ' ', '-')"/></xsl:when>
                <xsl:when test="contains(., 'Idempotent')">characteristic-<xsl:value-of select="translate(., ' ', '-')"/></xsl:when>
                <xsl:when test="contains(., 'Cacheable')">characteristic-<xsl:value-of select="translate(., ' ', '-')"/></xsl:when>
                <xsl:otherwise>characteristic</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <span class="characteristic {$char-class}">
            <xsl:value-of select="."/>
        </span>
    </xsl:template>

    <xsl:template name="get-method-badge">
        <xsl:param name="method"/>
        <xsl:choose>
            <xsl:when test="$method = 'GET'">
                <span class="badge badge-success">Safe</span>
            </xsl:when>
            <xsl:when test="$method = 'POST'">
                <span class="badge badge-danger">Create</span>
            </xsl:when>
            <xsl:when test="$method = 'PUT'">
                <span class="badge badge-warning">Update</span>
            </xsl:when>
            <xsl:when test="$method = 'DELETE'">
                <span class="badge badge-danger">Delete</span>
            </xsl:when>
            <xsl:when test="$method = 'PATCH'">
                <span class="badge badge-warning">Partial Update</span>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
