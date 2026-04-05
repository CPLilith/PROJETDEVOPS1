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

    const id = cardEl.dataset.id;
    const subject = cardEl.dataset.subject;
    const from = cardEl.dataset.from;
    const content = document.getElementById('content-' + id).value;

    renderReader(subject, "De : " + from, content, id, false);

    if (window.innerWidth <= 768) {
        document.getElementById('main-reader').scrollIntoView({ behavior: 'smooth' });
    }
}

function openNote(cardEl, event) {
    if (event) event.stopPropagation();
    document.querySelectorAll('.mail-card').forEach(c => c.classList.remove('active'));
    cardEl.classList.add('active');

    // CORRECTION : L'ID est maintenant l'UUID récupéré via dataset.id
    const id = cardEl.dataset.id;
    const subject = cardEl.dataset.subject;
    const from = cardEl.dataset.from;
    // On cherche le textarea qui a maintenant l'ID 'note-content-[UUID]'
    const content = document.getElementById('note-content-' + id).value;

    renderReader(subject, "Source : " + from, content, id, true);

    if (window.innerWidth <= 768) {
        document.getElementById('main-reader').scrollIntoView({ behavior: 'smooth' });
    }
}

// ==========================================
// MÉMOIRE DE DÉFILEMENT POUR MOBILE
// ==========================================
window.savedListScrollPosition = 0;

// ==========================================
// FONCTION D'AFFICHAGE DU MAIL
// ==========================================
function renderReader(title, sub, content, id, isNote) {
    // 1. Sauvegarde de la position AVANT d'afficher le mail
    if (window.innerWidth <= 1024) {
        window.savedListScrollPosition = window.scrollY || document.documentElement.scrollTop;
    }

    document.getElementById('default-placeholder')?.remove();
    const reader = document.getElementById('main-reader');

    // 2. Activation du mode lecture
    document.body.classList.add('reading-active');
    reader.style.display = 'block';

    const deleteFileBtn = isNote ? `<button onclick="deleteNote('${id}')" class="btn-delete"><i class="fas fa-trash"></i> Supprimer Note</button>` : '';

    let customTagsButtons = '';
    if (!isNote && window.jsCustomTags && window.jsCustomTags.length > 0) {
        customTagsButtons = '<div style="margin-top: 10px; display: flex; gap: 8px; flex-wrap: wrap; padding-top: 10px; border-top: 1px dashed var(--border);">';
        window.jsCustomTags.forEach(tag => {
            const displayLabel = tag.replace('DO_', '').replace('_', ' ');
            customTagsButtons += `<button onclick="upd('${id}', '${tag}', false)" class="tag-DO" style="opacity: 0.8;">DO · ${displayLabel}</button>`;
        });
        customTagsButtons += '</div>';
    }

    // 3. Injection du HTML (avec sécurités de débordement flex-wrap et break-word)
    reader.innerHTML = `
        <button class="mobile-back-btn" onclick="window.closeMobileReader()">
            <i class="fas fa-arrow-left"></i> Retour à la liste
        </button>

        <div style="display:flex; align-items:flex-start; justify-content:space-between; flex-wrap:wrap; gap:10px; margin-bottom:25px;">
            <div style="flex: 1; min-width: 0;">
                <h1 class="mail-title" style="margin:0; word-break: break-word; overflow-wrap: break-word;">${title}</h1>
                <p style="color:var(--text-sub); font-size: 15px; margin: 5px 0 0 0; word-break: break-word;">${sub}</p>
            </div>
            ${deleteFileBtn}
        </div>

        <div class="action-bar" style="background:#f8fafc; padding:20px; border-radius:16px; border:1px solid var(--border);">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; flex-wrap: wrap; gap: 10px;">
                <span style="font-size:11px; font-weight:800; color:var(--text-sub); letter-spacing: 0.5px;">ACTIONS EISENHOWER :</span>
                ${!isNote ? `
                <div style="display: flex; gap: 5px;">
                    <input type="text" id="quick-tag-input-${id}" placeholder="Nouveau sous-tag DO..." style="padding: 6px 10px; border-radius: 6px; border: 1px solid var(--border); font-size: 11px;">
                    <button onclick="createQuickTag('${id}')" style="padding: 6px 10px; font-size: 11px; background: white;"><i class="fas fa-plus"></i></button>
                </div>
                ` : ''}
            </div>
            
            <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                <button onclick="upd('${id}', 'DO', ${isNote})" class="tag-DO">DO (Générique)</button>
                <button onclick="upd('${id}', 'PLAN', ${isNote})" class="tag-PLAN">PLAN</button>
                ${!isNote ? `<button onclick="startDelegation('${id}')" class="tag-DELEGATE"><i class="fas fa-robot"></i> IA DELEGATE</button>` : ''}
                <button onclick="upd('${id}', 'DELETE', ${isNote})" class="tag-DELETE">DELETE</button>
            </div>
            ${customTagsButtons}
        </div>

        <div id="delegation-result-${id}" style="display:none; background: #fff; border: 1px solid var(--delegate); border-radius: 16px; padding: 20px; margin-top: 20px; box-shadow: 0 4px 12px rgba(245, 158, 11, 0.1);">
            <div style="font-size:11px; color:#b45309; font-weight:800; margin-bottom:10px; text-transform: uppercase;">
                🤖 Suggestion IA pour <span id="del-who-${id}">...</span>
            </div>
            <textarea id="del-draft-${id}" style="width:100%; height:120px; padding:15px; border:1px solid #cbd5e1; border-radius:10px; font-family:'Inter'; font-size:14px; color:#334155; line-height:1.6; resize:vertical; box-sizing:border-box;"></textarea>
            <div style="margin-top:15px; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px;">
                <span style="font-size:11px; color:var(--text-sub); font-family:monospace; background:#f1f5f9; padding:6px 10px; border-radius:6px; border: 1px solid var(--border); word-break: break-all;">Ref: <span id="del-id-${id}">...</span></span>
                <div style="display: flex; gap: 10px;">
                    <button onclick="document.getElementById('delegation-result-${id}').style.display='none'" style="background: white; color: var(--text-sub);">Annuler</button>
                    <button id="btn-confirm-${id}" style="display:none; background:#16a34a; color:white; border:none; padding:10px 20px; border-radius:10px; font-size:13px; font-weight:600;">
                        <i class="fas fa-paper-plane"></i> Valider
                    </button>
                </div>
            </div>
        </div>
        
        <div style="margin-top:30px; line-height:1.8; white-space:pre-wrap; word-break:break-word; overflow-wrap:break-word; font-size:15px; color:#334155; background: #fbfcfd; padding: 20px; border-radius: 16px; border: 1px solid var(--border); width: 100%; box-sizing: border-box; overflow-x: hidden;">${content}</div>
    `;

    // 4. Remonte tout en haut instantanément pour bien voir le bouton de retour
    if (window.innerWidth <= 1024) {
        setTimeout(function() {
            // Force le scroll tout en haut instantanément
            window.scrollTo({ top: 0, left: 0, behavior: 'instant' });
            
            // Sécurité supplémentaire pour certains navigateurs mobiles (Safari iOS)
            document.documentElement.scrollTop = 0;
            document.body.scrollTop = 0;
        }, 50); // 50 millisecondes de délai
    }
}

// ==========================================
// FONCTION POUR FERMER LE MAIL SUR MOBILE
// ==========================================
window.closeMobileReader = function () {
    // On enlève le mode lecture (la liste réapparaît via le CSS)
    document.body.classList.remove('reading-active');

    // On restaure instantanément la position de la liste
    window.scrollTo(0, window.savedListScrollPosition || 0);
};

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

    if (typeof closeMobileReader === 'function') {
        closeMobileReader();
    }
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