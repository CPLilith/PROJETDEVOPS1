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

    const id = cardEl.getAttribute('data-id');
    const subject = cardEl.getAttribute('data-subject');
    const from = cardEl.getAttribute('data-from');
    const content = document.getElementById('content-' + id).value;

    renderReader(subject, "De : " + from, content, id, false);
}

function openNote(cardEl, event) {
    if (event) event.stopPropagation();
    document.querySelectorAll('.mail-card').forEach(c => c.classList.remove('active'));
    cardEl.classList.add('active');

    const id = cardEl.getAttribute('data-id');
    const subject = cardEl.getAttribute('data-subject');
    const from = cardEl.getAttribute('data-from');
    const content = document.getElementById('note-content-' + id).value;

    renderReader(subject, "Auteur IA : " + from, content, id, true);
}

function renderReader(title, sub, content, id, isNote) {
    document.getElementById('default-placeholder')?.remove();

    const deleteFileBtn = isNote ? `<button onclick="deleteNote('${id}')" class="btn-delete"><i class="fas fa-trash"></i> Supprimer Note</button>` : '';

    let customTagsButtons = '';
    // Utilisation de la variable globale injectée par Thymeleaf
    if (!isNote && window.jsCustomTags && window.jsCustomTags.length > 0) {
        customTagsButtons = '<div style="margin-top: 10px; display: flex; gap: 8px; flex-wrap: wrap; padding-top: 10px; border-top: 1px dashed var(--border);">';
        window.jsCustomTags.forEach(tag => {
            const displayLabel = tag.replace('DO_', '').replace('_', ' ');
            customTagsButtons += `<button onclick="upd('${id}', '${tag}', false)" class="tag-DO" style="opacity: 0.8;">DO · ${displayLabel}</button>`;
        });
        customTagsButtons += '</div>';
    }

    document.getElementById('main-reader').innerHTML = `
        <div style="display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:25px;">
            <div>
                <h1 class="mail-title">${title}</h1>
                <p style="color:var(--text-sub); font-size: 15px; margin: 0;">${sub}</p>
            </div>
            ${deleteFileBtn}
        </div>

        <div class="action-bar" style="background:#f8fafc; padding:20px; border-radius:16px; border:1px solid var(--border);">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
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
            <div style="margin-top:15px; display:flex; justify-content:space-between; align-items:center;">
                <span style="font-size:11px; color:var(--text-sub); font-family:monospace; background:#f1f5f9; padding:6px 10px; border-radius:6px; border: 1px solid var(--border);">Ref: <span id="del-id-${id}">...</span></span>
                <div style="display: flex; gap: 10px;">
                    <button onclick="document.getElementById('delegation-result-${id}').style.display='none'" style="background: white; color: var(--text-sub);">Annuler</button>
                    <button id="btn-confirm-${id}" style="display:none; background:#16a34a; color:white; border:none; padding:10px 20px; border-radius:10px; font-size:13px; font-weight:600;">
                        <i class="fas fa-paper-plane"></i> Valider & Créer Brouillon
                    </button>
                </div>
            </div>
        </div>
        
        <div style="margin-top:40px; line-height:1.8; white-space:pre-wrap; font-size:15px; color:#334155; background: #fbfcfd; padding: 30px; border-radius: 16px; border: 1px solid var(--border);">${content}</div>
    `;
}

function deleteNote(index) {
    if (confirm("Supprimer définitivement ce résumé de la base de connaissances ?")) {
        showLoader("Suppression en cours...");
        const f = document.createElement('form');
        f.method = 'POST'; f.action = '/knowledge/delete';
        f.innerHTML = `<input type="hidden" name="index" value="${index}">`;
        document.body.appendChild(f); f.submit();
    }
}

function upd(id, tag, isNote) {
    showLoader("Mise à jour du tag...");
    const f = document.createElement('form');
    f.method = 'POST'; f.action = isNote ? '/update-note-tag' : '/update-mail-tag';
    f.innerHTML = `<input type="hidden" name="${isNote ? 'index' : 'messageId'}" value="${id}"><input type="hidden" name="tag" value="${tag}">`;
    document.body.appendChild(f); f.submit();
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
        .catch(err => {
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
        .then(response => response.json())
        .then(data => {
            document.getElementById('del-who-' + messageId).innerText = data.assignee;
            document.getElementById('del-draft-' + messageId).value = data.draftBody;
            document.getElementById('del-id-' + messageId).innerText = data.trackingId;

            const btnConfirm = document.getElementById('btn-confirm-' + messageId);
            btnConfirm.style.display = 'block';

            btnConfirm.onclick = function () {
                const finalDraft = document.getElementById('del-draft-' + messageId).value;
                confirmDelegationAction(messageId, data.assignee, finalDraft);
            };
        })
        .catch(err => {
            document.getElementById('del-draft-' + messageId).value = "Erreur lors de la communication avec l'IA.";
        });
}

function confirmDelegationAction(messageId, assignee, draftBody) {
    showLoader("Création du brouillon côté serveur...");
    fetch('/delegate-confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'messageId=' + encodeURIComponent(messageId) + '&assignee=' + encodeURIComponent(assignee) + '&draftBody=' + encodeURIComponent(draftBody)
    })
        .then(res => res.json())
        .then(data => {
            window.location.href = "/kanban";
        });
}

function generateMemo(id, btn) {
    showLoader("Génération du rapport PDF via IA...");
    fetch('/events/prepare', { method: 'POST', body: new URLSearchParams({ 'messageId': id }) })
        .then(r => r.blob()).then(blob => {
            const a = document.createElement('a');
            a.href = window.URL.createObjectURL(blob);
            a.download = "Fiche_Preparation.pdf";
            a.className = "btn-primary";
            a.innerHTML = "<i class='fas fa-file-pdf'></i> Télécharger PDF";
            a.style.cssText = `display: inline-flex; align-items: center; justify-content: center; gap: 8px; padding: 10px 20px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 14px; background: var(--text-main); color: white; border: none; cursor: pointer; transition: background 0.2s;`;
            btn.parentNode.replaceChild(a, btn);
            document.getElementById('loading-overlay').style.display = 'none';
        });
}

function setFilter(tag, chipEl) {
    document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
    chipEl.classList.add('active');

    document.querySelectorAll('.feed-list .mail-card').forEach(card => {
        const cardTag = card.getAttribute('data-tag') || 'PENDING';
        let show = false;

        if (tag === 'ALL') {
            show = true;
        } else if (tag === 'PENDING') {
            show = (cardTag === 'PENDING' || cardTag === '' || cardTag === 'null');
        } else if (tag === 'DO') {
            show = (cardTag === 'DO');
        } else {
            show = (cardTag === tag);
        }

        if (show) {
            card.classList.remove('hidden');
        } else {
            card.classList.add('hidden');
        }
    });
}

function openDeleteModal(tagName) {
    // On prépare le formulaire avec le nom du tag
    document.getElementById('modal-tag-name').value = tagName;
    // On affiche la modale avec un effet flex
    document.getElementById('delete-modal').style.display = 'flex';
}

function closeDeleteModal() {
    document.getElementById('delete-modal').style.display = 'none';
}