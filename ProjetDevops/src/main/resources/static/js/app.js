function showLoader(msg) {
    document.getElementById('loader-message').innerText = msg;
    document.getElementById('loading-overlay').style.display = 'flex';
}

function switchPersona(p) {
    document.getElementById('selectedPersona').value = p;
    showLoader("Changement de profil...");
    document.getElementById('personaForm').submit();
}

function openMail(cardEl, event) {
    if (event) event.stopPropagation();
    document.querySelectorAll('.mail-card').forEach(c => c.classList.remove('active'));
    cardEl.classList.add('active');
    const id      = cardEl.dataset.id;
    const subject = cardEl.dataset.subject;
    const from    = cardEl.dataset.from;
    const content = document.getElementById('content-' + id).value;
    renderReader(subject, "De : " + from, content, id, false);
}

function openNote(cardEl, event) {
    if (event) event.stopPropagation();
    document.querySelectorAll('.mail-card').forEach(c => c.classList.remove('active'));
    cardEl.classList.add('active');
    const id      = cardEl.dataset.id;
    const titleEl = cardEl.querySelector('.card-subject');
    const subject = titleEl ? titleEl.textContent.trim() : (cardEl.dataset.subject || 'Sans titre');
    const from    = cardEl.dataset.from || '';
    const content = document.getElementById('note-content-' + id).value;
    renderReader(subject, from, content, id, true);
}

function renderReader(title, sub, content, id, isNote) {
    document.getElementById('default-placeholder')?.remove();

    const deleteFileBtn = isNote
        ? `<button onclick="deleteNote('${id}')" class="btn-delete">
               <i class="fas fa-trash"></i> Supprimer Note
           </button>`
        : '';

    let customTagsButtons = '';
    if (!isNote && window.jsCustomTags && window.jsCustomTags.length > 0) {
        customTagsButtons = `
            <div style="margin-top:10px; display:flex; gap:8px; flex-wrap:wrap;
                        padding-top:10px; border-top:1px dashed var(--border);">`;
        window.jsCustomTags.forEach(tag => {
            const displayLabel = tag.replace('DO_', '').replace('_', ' ');
            customTagsButtons += `
                <button onclick="upd('${id}','${tag}',false)"
                        class="tag-DO"
                        style="opacity:0.8;">
                    DO · ${displayLabel}
                </button>`;
        });
        customTagsButtons += `
            </div>`;
    }

    const authorSection = isNote
        ? `<div style="display:flex; align-items:center; gap:8px; margin-top:6px;">
               <span style="color:var(--text-sub); font-size:13px;">Auteur :</span>
               <input type="text"
                      id="note-author-${id}"
                      value="${sub}"
                      placeholder="Ajouter un auteur..."
                      style="border:1px solid var(--border); border-radius:8px; padding:4px 10px;
                             font-size:13px; color:var(--text-main); background:#fff;
                             font-family:Inter; width:200px;">
           </div>`
        : `<p style="color:var(--text-sub); font-size:15px; margin:0;">${sub}</p>`;

    const quickTagInput = !isNote
        ? `<div style="display:flex; gap:5px;">
               <input type="text"
                      id="quick-tag-input-${id}"
                      placeholder="Nouveau sous-tag DO..."
                      style="padding:6px 10px; border-radius:6px; border:1px solid var(--border); font-size:11px;">
               <button onclick="createQuickTag('${id}')"
                       style="padding:6px 10px; font-size:11px; background:white;">
                   <i class="fas fa-plus"></i>
               </button>
           </div>`
        : '';

    const delegateBtn = !isNote
        ? `<button onclick="startDelegation('${id}')" class="tag-DELEGATE">
               <i class="fas fa-robot"></i> IA DELEGATE
           </button>`
        : '';

    document.getElementById('main-reader').innerHTML = `
        <div style="display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:25px;">
            <div>
                <h1 class="mail-title">${title}</h1>
                ${authorSection}
            </div>
            ${deleteFileBtn}
        </div>

        <div class="action-bar"
             style="background:#f8fafc; padding:20px; border-radius:16px; border:1px solid var(--border);">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:15px;">
                <span style="font-size:11px; font-weight:800; color:var(--text-sub); letter-spacing:0.5px;">
                    ACTIONS EISENHOWER :
                </span>
                ${quickTagInput}
            </div>
            <div style="display:flex; gap:10px; flex-wrap:wrap;">
                <button onclick="upd('${id}','DO',${isNote})" class="tag-DO">DO (Générique)</button>
                <button onclick="upd('${id}','PLAN',${isNote})" class="tag-PLAN">PLAN</button>
                ${delegateBtn}
                <button onclick="upd('${id}','DELETE',${isNote})" class="tag-DELETE">DELETE</button>
            </div>
            ${customTagsButtons}
        </div>

        <div id="delegation-result-${id}"
             style="display:none; background:#fff; border:1px solid var(--delegate);
                    border-radius:16px; padding:20px; margin-top:20px;">
            <div style="font-size:11px; color:#b45309; font-weight:800;
                        margin-bottom:10px; text-transform:uppercase;">
                🤖 Suggestion IA pour <span id="del-who-${id}">...</span>
            </div>
            <textarea id="del-draft-${id}"
                      style="width:100%; height:120px; padding:15px; border:1px solid #cbd5e1;
                             border-radius:10px; font-family:'Inter'; font-size:14px;
                             color:#334155; line-height:1.6; resize:vertical; box-sizing:border-box;">
            </textarea>
            <div style="margin-top:15px; display:flex; justify-content:space-between; align-items:center;">
                <span style="font-size:11px; color:var(--text-sub); font-family:monospace;
                             background:#f1f5f9; padding:6px 10px; border-radius:6px; border:1px solid var(--border);">
                    Ref: <span id="del-id-${id}">...</span>
                </span>
                <div style="display:flex; gap:10px;">
                    <button onclick="document.getElementById('delegation-result-${id}').style.display='none'"
                            style="background:white; color:var(--text-sub);">
                        Annuler
                    </button>
                    <button id="btn-confirm-${id}"
                            style="display:none; background:#16a34a; color:white; border:none;
                                   padding:10px 20px; border-radius:10px; font-size:13px; font-weight:600;">
                        <i class="fas fa-paper-plane"></i> Valider & Créer Brouillon
                    </button>
                </div>
            </div>
        </div>

        <div style="margin-top:40px; line-height:1.8; white-space:pre-wrap; font-size:15px;
                    color:#334155; background:#fbfcfd; padding:30px; border-radius:16px;
                    border:1px solid var(--border);">
            ${content}
        </div>
    `;
}

function deleteNote(index) {
    if (confirm("Supprimer définitivement ce résumé de la base de connaissances ?")) {
        showLoader("Suppression en cours...");
        const f = document.createElement('form');
        f.method = 'POST';
        f.action = '/knowledge/delete';
        f.innerHTML = `<input type="hidden" name="index" value="${index}">`;
        document.body.appendChild(f);
        f.submit();
    }
}

function upd(id, tag, isNote) {
    showLoader("Mise à jour du tag...");
    const f = document.createElement('form');
    f.method = 'POST';
    f.action = isNote ? '/update-note-tag' : '/update-mail-tag';
    f.innerHTML = `
        <input type="hidden" name="${isNote ? 'index' : 'messageId'}" value="${id}">
        <input type="hidden" name="tag" value="${tag}">
    `;
    document.body.appendChild(f);
    f.submit();
}

function createQuickTag(messageId) {
    const input = document.getElementById('quick-tag-input-' + messageId);
    const label = input.value.trim();
    if (!label) return;

    input.disabled = true;
    input.value = "Création...";

    fetch('/tags/create-ajax', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'label=' + encodeURIComponent(label)
    })
        .then(res => res.json())
        .then(data => {
            if (data.error) {
                alert(data.error);
                input.disabled = false;
                input.value = label;
            } else {
                upd(messageId, data.tag, false);
            }
        })
        .catch(() => {
            alert("Erreur de connexion");
            input.disabled = false;
        });
}

function startDelegation(messageId) {
    const resultDiv = document.getElementById('delegation-result-' + messageId);
    resultDiv.style.display = 'block';
    document.getElementById('del-who-' + messageId).innerText = "Recherche dans l'équipe...";
    document.getElementById('del-draft-' + messageId).value = "Analyse en cours...";

    fetch('/delegate-auto', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'messageId=' + encodeURIComponent(messageId)
    })
        .then(r => r.json())
        .then(data => {
            document.getElementById('del-who-' + messageId).innerText = data.assignee;
            document.getElementById('del-draft-' + messageId).value   = data.draftBody;
            document.getElementById('del-id-' + messageId).innerText  = data.trackingId;

            const btn = document.getElementById('btn-confirm-' + messageId);
            btn.style.display = 'block';
            btn.onclick = () => confirmDelegationAction(
                messageId,
                data.assignee,
                document.getElementById('del-draft-' + messageId).value
            );
        })
        .catch(() => {
            document.getElementById('del-draft-' + messageId).value = "Erreur IA.";
        });
}

function confirmDelegationAction(messageId, assignee, draftBody) {
    showLoader("Création du brouillon...");
    fetch('/delegate-confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'messageId=' + encodeURIComponent(messageId)
            + '&assignee=' + encodeURIComponent(assignee)
            + '&draftBody=' + encodeURIComponent(draftBody)
    })
        .then(r => r.json())
        .then(() => {
            window.location.href = "/kanban";
        });
}

function setFilter(tag, chipEl) {
    document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
    chipEl.classList.add('active');

    document.querySelectorAll('.feed-list .mail-card').forEach(card => {
        const cardTag = card.dataset.tag || 'PENDING';
        let show;

        if (tag === 'ALL') {
            show = true;
        } else if (tag === 'PENDING') {
            show = cardTag === 'PENDING' || cardTag === '' || cardTag === 'null';
        } else if (tag === 'DO') {
            show = cardTag === 'DO';
        } else {
            show = cardTag === tag;
        }

        card.classList.toggle('hidden', !show);
    });
}

function openDeleteModal(tagName) {
    document.getElementById('modal-tag-name').value = tagName;
    document.getElementById('delete-modal').style.display = 'flex';
}

function closeDeleteModal() {
    document.getElementById('delete-modal').style.display = 'none';
}

// ========== SIDEBAR TOGGLE ==========

function toggleSidebar() {
    const sidebar   = document.getElementById('main-sidebar');
    const app       = document.querySelector('.app-container');
    const revealBtn = document.getElementById('sidebar-reveal-btn');

    // En mode topbar, le bouton collapse est masqué donc on ne fait rien
    if (app.classList.contains('topbar-mode')) return;

    const isOpen = !sidebar.classList.contains('collapsed');

    if (isOpen) {
        sidebar.classList.add('collapsed');
        app.classList.add('sidebar-hidden');
        revealBtn.classList.add('visible');
    } else {
        sidebar.classList.remove('collapsed');
        app.classList.remove('sidebar-hidden');
        revealBtn.classList.remove('visible');
    }
}

function toggleSidebarLayout() {
    const app = document.querySelector('.app-container');
    app.classList.toggle('topbar-mode');
    localStorage.setItem(
        'eisenflow_layout',
        app.classList.contains('topbar-mode') ? 'topbar' : 'sidebar'
    );
}

// Au chargement : restaurer seulement le mode topbar
document.addEventListener('DOMContentLoaded', function () {
    if (localStorage.getItem('eisenflow_layout') === 'topbar') {
        document.querySelector('.app-container')?.classList.add('topbar-mode');
    }
    const sidebar   = document.getElementById('main-sidebar');
    const app       = document.querySelector('.app-container');
    const revealBtn = document.getElementById('sidebar-reveal-btn');
    if (sidebar)   sidebar.classList.remove('collapsed');
    if (app)       app.classList.remove('sidebar-hidden');
    if (revealBtn) revealBtn.classList.remove('visible');
});