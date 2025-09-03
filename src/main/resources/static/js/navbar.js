document.addEventListener('DOMContentLoaded', () => {
  const usernameBox = document.getElementById('username-box');
  const dropdown = document.getElementById('logoutDropdown');

  if (usernameBox && dropdown) {
    // Toggle dropdown on click
    usernameBox.addEventListener('click', (e) => {
      e.stopPropagation();
      dropdown.classList.toggle('show');
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', () => {
      dropdown.classList.remove('show');
    });

    // Prevent closing when clicking inside dropdown
    dropdown.addEventListener('click', (e) => {
      e.stopPropagation();
    });
  }
});
