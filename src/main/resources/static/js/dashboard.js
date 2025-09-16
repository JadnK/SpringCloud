    const burger = document.getElementById('burger');
    const mobileMenu = document.getElementById('mobileMenu');

    const uploadPopup = document.getElementById('uploadPopup');
    const editModal = document.getElementById('editModal');
    const editFileIdInput = document.getElementById('editFileId');

    const previewPopup = document.getElementById('previewPopup');
    const previewContainer = document.getElementById('previewContainer');

    const sharePopup = document.getElementById('confirmSharePopup');
    let currentShareFileId = null;

    const deletePopup = document.getElementById('confirmDeletePopup');
    let currentDeleteFileId = null;

    const fileList = document.getElementById('fileList');
    const paginationContainer = document.getElementById('pagination');
    const itemsPerPage = 10;
    let currentPage = 1;
    let fileItems = Array.from(fileList.children);

    // --- Burger Menu ---
    burger.addEventListener('click', () => {
        mobileMenu.classList.toggle('hidden');
        mobileMenu.classList.toggle('flex');
    });

    function showUploadPopup() { uploadPopup.classList.remove('hidden'); }
    function closeUploadPopup() { uploadPopup.classList.add('hidden'); }

function showEditPopup(fileId) {
    const editFileIdInput = document.getElementById('editFileId');

    editFileIdInput.value = fileId;

    editModal.classList.remove('hidden');

     fetch(`/springcloud/api/file/${fileId}/authorizedUsers`)
         .then(res => res.json())
         .then(authorizedUserIds => {
             const userList = editModal.querySelectorAll('.user-list li');
             userList.forEach(li => {
                 const checkbox = li.querySelector('input[type="checkbox"]');
                 if (authorizedUserIds.includes(parseInt(checkbox.value))) {
                     checkbox.checked = true;
                 } else {
                     checkbox.checked = false;
                 }
             });
         });
}

    function closeEditPopup() { editModal.classList.add('hidden'); }

        const folderPopup = document.getElementById('folderPopup');

    function showFolderPopup() { folderPopup.classList.remove('hidden'); }
    function closeFolderPopup() { folderPopup.classList.add('hidden'); }

function showPreviewPopup(fileId, fileType, fileName) {
    previewContainer.innerHTML = '';
    if(fileType.startsWith('image')) {
        const img = document.createElement('img');
        img.src = '/springcloud/file/' + fileId;
        img.className = 'w-auto max-w-full h-auto max-h-[70vh] object-contain';
        previewContainer.appendChild(img);
    } else if(fileType.startsWith('video')) {
        const video = document.createElement('video');
        video.src = '/springcloud/file/' + fileId;
        video.controls = true;
        video.className = 'w-auto max-w-full h-auto max-h-[70vh]';
        previewContainer.appendChild(video);
    } else if(fileType === 'application/pdf') {
        const iframe = document.createElement('iframe');
        iframe.src = '/springcloud/file/' + fileId;
        iframe.className = 'w-full h-[70vh]';
        previewContainer.appendChild(iframe);
    } else {
        const pre = document.createElement('pre');
        pre.className = 'max-h-[70vh] overflow-auto';
        fetch('/springcloud/file/' + fileId).then(r => r.text()).then(t => pre.innerText = t);
        previewContainer.appendChild(pre);
    }
    previewPopup.classList.remove('hidden');
}

    function closePreviewPopup() { previewPopup.classList.add('hidden'); }

    function confirmShare(fileId) { currentShareFileId = fileId; sharePopup.classList.remove('hidden'); }
    function closeConfirmSharePopup() { currentShareFileId = null; sharePopup.classList.add('hidden'); }
    document.getElementById('confirmShareBtn').addEventListener('click', () => {
        if(currentShareFileId) {
            const duration = document.getElementById('shareDurationSelect').value;
            window.location.href = `/springcloud/share/${currentShareFileId}?duration=${duration}`;
        }
    });

    function confirmDeleteFromEdit() { currentDeleteFileId = editFileIdInput.value; editModal.classList.add('hidden'); deletePopup.classList.remove('hidden'); }
    function closeConfirmDeletePopup() { currentDeleteFileId = null; deletePopup.classList.add('hidden'); }
    document.getElementById('confirmDeleteBtn').addEventListener('click', () => {
        if(currentDeleteFileId) { window.location.href = `/springcloud/delete/${currentDeleteFileId}`; }
    });

function filterFiles() {
    const filter = document.getElementById('fileSearch').value.toLowerCase();
    const selectedFolder = document.getElementById('folderSelectTop').value;

    const allItems = Array.from(fileList.children);
    allItems.forEach(tr => {
        const text = tr.innerText.toLowerCase();
        const folderId = tr.getAttribute('data-folder-id') || ''; // Stelle sicher, dass jede TR folder-id hat
        const matchesFilter = text.includes(filter);
const matchesFolder = selectedFolder === '' || Number(folderId) === Number(selectedFolder);
        tr.style.display = (matchesFilter && matchesFolder) ? '' : 'none';
    });

    if (window.innerWidth >= 768) {
        fileItems = allItems.filter(tr => tr.style.display !== 'none');
        showPage(1);
    }

    // Mobile
    document.querySelectorAll('#fileListMobile .file-card').forEach(card => {
        const text = card.innerText.toLowerCase();
        const folderId = card.getAttribute('data-folder-id') || '';
        const matchesFilter = text.includes(filter);
const matchesFolder = selectedFolder === '' || Number(folderId) === Number(selectedFolder);
        card.style.display = (matchesFilter && matchesFolder) ? '' : 'none';
    });
}

function filterUsers() {
    const filter = document.getElementById('userSearchInput').value.toLowerCase();
    document.querySelectorAll('.user-list li').forEach(li => {
        const username = li.querySelector('span').innerText.toLowerCase();
        li.style.display = username.includes(filter) ? '' : 'none';
    });
}

    function showPage(page) {
        currentPage = page;
        const start = (page - 1) * itemsPerPage;
        const end = start + itemsPerPage;
        fileItems.forEach((li, index) => { li.style.display = (index >= start && index < end) ? '' : 'none'; });
        renderPagination();
    }

    function renderPagination() {
        if (window.innerWidth < 768) {
            paginationContainer.innerHTML = '';
            return;
        }

        const totalPages = Math.ceil(fileItems.length / itemsPerPage);
        paginationContainer.innerHTML = '';
        for(let i=1; i<=totalPages; i++) {
            const btn = document.createElement('button');
            btn.textContent = i;
            btn.className = `px-3 py-1 rounded ${i === currentPage
                ? 'bg-[var(--color-primary)] text-white'
                : 'bg-[var(--color-bg-alt)] text-[var(--color-text)] hover:bg-[var(--color-primary-dark)]'}`;
            btn.addEventListener('click', () => showPage(i));
            paginationContainer.appendChild(btn);
        }
    }


    function toggleFileMenu(button) {
    const menu = button.nextElementSibling;

    document.querySelectorAll('#fileListMobile .file-menu').forEach(m => {
        if (m !== menu) {
            m.classList.add('hidden');
            m.classList.remove('opacity-100', 'scale-100');
            m.classList.add('opacity-0', 'scale-95');
        }
    });

    if (menu.classList.contains('hidden')) {
        menu.classList.remove('hidden');
        setTimeout(() => {
            menu.classList.remove('opacity-0', 'scale-95');
            menu.classList.add('opacity-100', 'scale-100');
        }, 10);
    } else {
        menu.classList.add('opacity-0', 'scale-95');
        menu.classList.remove('opacity-100', 'scale-100');
        setTimeout(() => menu.classList.add('hidden'), 200);
    }
}

document.addEventListener('click', (e) => {
    if (!e.target.closest('#fileListMobile')) {
        document.querySelectorAll('#fileListMobile .file-menu').forEach(m => {
            m.classList.add('hidden');
            m.classList.remove('opacity-100', 'scale-100');
            m.classList.add('opacity-0', 'scale-95');
        });
    }
});

function showDeleteFolderPopup() {
    const select = document.getElementById('folderSelectTop');
    const folderIdInput = document.getElementById('deleteFolderId');

    if (!select.value) {
        alert("Please select a folder to delete.");
        return;
    }

    folderIdInput.value = select.value; // dynamisch die folderId setzen
    document.getElementById('deleteFolderPopup').classList.remove('hidden');
}

function closeDeleteFolderPopup() {
    document.getElementById('deleteFolderPopup').classList.add('hidden');
}


showPage(1);