<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" encoding="UTF-8" indent="yes"/>

	<xsl:template match="/">
		<html>
			<head>
				<meta charset="UTF-8"/>
				<title>Friends List</title>
				<link rel="stylesheet" href="../properties/css/friends.css"/>
			</head>
			<body>
				<h2>Friends List</h2>

				<table class="friends">
					<tr bgcolor="teal">
						<th>ID</th>
						<th>First Name</th>
						<th>Last Name</th>
						<th>Phone Number</th>
						<th>Date of Birth</th>
						<th>Address</th>
						<th>Email</th>
					</tr>

					<xsl:for-each select="friendList/friend">
						<tr>
							<td class="id">
								<xsl:value-of select="@id"/>
							</td>
							<td>
								<xsl:value-of select="firstName"/>
							</td>
							<td>
								<xsl:value-of select="lastName"/>
							</td>
							<td>
								<xsl:value-of select="phoneNumber"/>
							</td>
							<td>
								<xsl:value-of select="concat(dateOfBirth/day, ' ', dateOfBirth/month, ' ', dateOfBirth/year)"/>
							</td>
							<td>
								<xsl:value-of select="concat(address/street, ', ', address/building, ', ', address/apartment)"/>
							</td>
							<td>
								<xsl:value-of select="email"/>
							</td>
						</tr>
					</xsl:for-each>
				</table>

				<script src="../properties/script/friends.js"></script>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
