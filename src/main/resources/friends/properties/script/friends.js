document.addEventListener("DOMContentLoaded", () => {
	const rows = document.querySelectorAll("table.friends tr");

	rows.forEach((row, index) => {
		if (index === 0) return; // пропускаем заголовок
		row.addEventListener("click", () => {
			const name = row.children[1].textContent + " " + row.children[2].textContent;
			alert(`👋 Привет, ${name}!`);
		});
	});
});
