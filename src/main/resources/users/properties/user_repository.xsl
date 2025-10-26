<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" encoding="UTF-8" indent="yes"/>

	<xsl:template match="/">
		<html>
			<head>
				<meta charset="UTF-8"/>
				<title>User Repository</title>
				<link rel="stylesheet" href="../properties/styles/styles.css"/>
			</head>
			<body>
				<div class="container">
					<header>
						<h1>User Repository</h1>
						<div class="stats">
							<span>Total Users: <xsl:value-of select="count(userRepository/users/user)"/>
							</span>
						</div>
					</header>

					<div class="controls">
						<input type="text" id="searchInput" placeholder="Search users..." class="search-input"/>
						<button onclick="sortTable('firstName')" class="btn">Sort by First Name</button>
						<button onclick="sortTable('lastName')" class="btn">Sort by Last Name</button>
						<button onclick="resetView()" class="btn btn-reset">Reset</button>
					</div>

					<table id="usersTable" class="users-table">
						<thead>
							<tr>
								<th data-sort="id">ID</th>
								<th data-sort="firstName">First Name</th>
								<th data-sort="lastName">Last Name</th>
								<th data-sort="email">Email</th>
								<th>Address</th>
								<th>Actions</th>
							</tr>
						</thead>
						<tbody>
							<xsl:for-each select="userRepository/users/user">
								<tr class="user-row">
									<td class="user-id">
										<xsl:value-of select="@id"/>
									</td>
									<td class="first-name">
										<xsl:value-of select="firstName"/>
									</td>
									<td class="last-name">
										<xsl:value-of select="lastName"/>
									</td>
									<td class="email">
										<a href="mailto:{email}">
											<xsl:value-of select="email"/>
										</a>
									</td>
									<td class="address">
										<xsl:value-of select="concat(address/street, ' st., ', address/building, ', apt. ', address/apartment)"/>
									</td>
									<td class="actions">
										<button class="btn btn-view" onclick="viewUser('{@id}')">View</button>
										<button class="btn btn-edit" onclick="editUser('{@id}')">Edit</button>
									</td>
								</tr>
							</xsl:for-each>
						</tbody>
					</table>

					<div id="userDetails" class="user-details" style="display: none;">
						<h3>User Details</h3>
						<div id="detailsContent"></div>
						<button onclick="closeDetails()" class="btn btn-close">Close</button>
					</div>
				</div>

				<script src="../properties/scripts/script.js"></script>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
