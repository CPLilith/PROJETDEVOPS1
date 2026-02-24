package projet.devops.Mail.Controller;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Classifier.EisenhowerClassifier;
import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Classifier.PersonaResourceService;
import projet.devops.Mail.Mail;
import projet.devops.Mail.Model.Note;
import projet.devops.Mail.Service.CustomDoTagService;
import projet.devops.Mail.Service.MailFlowService;
import projet.devops.Mail.Service.MailFlowService.DelegationData;
import projet.devops.Mail.Service.MeetingPrepService;
import projet.devops.Mail.Service.NoteService;

record EventItem(String title, String dateLieu, String type, String sourceId) {
}

record RestResponse<T>(T data, Map<String, String> _links) {
}

@Controller
public class MainController {

    private final MailFlowService flowService;
    private final NoteService noteService;
    private final EisenhowerClassifier classifier;
    private final MeetingPrepService meetingPrepService;
    private final CustomDoTagService customDoTagService;

    public MainController(MailFlowService flowService, NoteService noteService,
            EisenhowerClassifier classifier, MeetingPrepService meetingPrepService,
            CustomDoTagService customDoTagService) {
        this.flowService = flowService;
        this.noteService = noteService;
        this.classifier = classifier;
        this.meetingPrepService = meetingPrepService;
        this.customDoTagService = customDoTagService;
    }

    private void addPersonaToModel(Model model) {
        try {
            model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        } catch (Exception e) {
            model.addAttribute("currentPersona", Persona.ETUDIANT);
        }
    }

    private void addTagsToModel(Model model) {
        List<String> customTags = customDoTagService.getCustomTags();
        model.addAttribute("customDoTags", customTags);
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("view", "inbox");
        List<Mail> currentMails = flowService.getMails();
        if (currentMails == null)
            currentMails = new ArrayList<>();
        List<Mail> sortedMails = new ArrayList<>(currentMails);
        if (!sortedMails.isEmpty())
            Collections.reverse(sortedMails);
        model.addAttribute("mails", sortedMails);
        addPersonaToModel(model);
        addTagsToModel(model);
        return "mails";
    }

    @GetMapping("/kanban")
    public String kanban(Model model) {
        List<Mail> mails = flowService.getMails();
        if (mails == null)
            mails = new ArrayList<>();
        model.addAttribute("todoMails", mails.stream()
                .filter(m -> m.getAction() == EisenhowerAction.DELEGATE && !"FINALISÉ".equals(m.getStatus())).toList());
        model.addAttribute("doneMails", mails.stream()
                .filter(m -> "FINALISÉ".equals(m.getStatus())).toList());
        model.addAttribute("view", "kanban");
        addPersonaToModel(model);
        addTagsToModel(model);
        return "mails";
    }

    @GetMapping("/knowledge")
    public String knowledge(Model model) {
        model.addAttribute("view", "knowledge");
        List<Note> sortedNotes = new ArrayList<>(noteService.getNotes());
        Collections.reverse(sortedNotes);
        model.addAttribute("notes", sortedNotes);
        addPersonaToModel(model);
        addTagsToModel(model);
        return "mails";
    }

    @GetMapping("/events")
    public String showEvents(Model model) {
        List<EventItem> events = new ArrayList<>();
        if (flowService.getMails().isEmpty()) {
            try {
                flowService.fetchMails();
            } catch (Exception e) {
            }
        }
        for (Mail m : flowService.getMails()) {
            if (m.getAction() == EisenhowerAction.PLAN) {
                String ex = classifier.extractEventDetails(m.getContent());
                String displayDate = ex.contains("AUCUN") ? "⚠️ À Planifier (Urgent)" : ex;
                events.add(new EventItem(m.getSubject(), displayDate, "PLAN", m.getMessageId()));
            } else if (m.getAction().isDo()) {
                String ex = classifier.extractEventDetails(m.getContent());
                String displayDate = ex.contains("AUCUN") ? "⚠️ À faire" : ex;
                events.add(new EventItem(m.getSubject(), displayDate, m.getEffectiveTag(), m.getMessageId()));
            }
        }
        Collections.reverse(events);
        model.addAttribute("view", "events");
        model.addAttribute("events", events);
        addPersonaToModel(model);
        addTagsToModel(model);
        return "mails";
    }

    @GetMapping("/tags")
    public String tagsPage(Model model) {
        model.addAttribute("view", "tags");
        addTagsToModel(model);
        addPersonaToModel(model);
        return "mails";
    }

    @PostMapping("/tags/create")
    public String createTag(@RequestParam("label") String label, Model model) {
        String created = customDoTagService.createTag(label);
        if (created == null) {
            model.addAttribute("tagError", "Ce tag existe déjà ou le nom est invalide.");
        } else {
            model.addAttribute("tagSuccess", "Tag créé : " + CustomDoTagService.toLabel(created));
        }
        model.addAttribute("view", "tags");
        addTagsToModel(model);
        addPersonaToModel(model);
        return "mails";
    }

    @PostMapping("/tags/delete")
    public String deleteTag(@RequestParam("tagName") String tagName) {
        customDoTagService.deleteTag(tagName);
        return "redirect:/tags";
    }

    @GetMapping("/api/tags")
    @ResponseBody
    public List<Map<String, String>> apiGetTags() {
        return customDoTagService.getCustomTags().stream()
                .map(t -> Map.of("name", t, "label", CustomDoTagService.toLabel(t)))
                .toList();
    }

    @PostMapping("/tags/create-ajax")
    @ResponseBody
    public Map<String, String> createTagAjax(@RequestParam("label") String label) {
        String created = customDoTagService.createTag(label);
        if (created == null) {
            return Map.of("error", "Tag déjà existant ou nom invalide.");
        }
        return Map.of("tag", created, "label", CustomDoTagService.toLabel(created));
    }

    @PostMapping("/knowledge/upload")
    public String uploadNotes(@RequestParam("files") MultipartFile[] files) throws Exception {
        noteService.generateAiKnowledge(files, PersonaResourceService.loadPersona());
        return "redirect:/knowledge";
    }

    @PostMapping("/delegate-auto")
    @ResponseBody
    public DelegationData delegateAuto(@RequestParam String messageId) {
        return flowService.suggestDelegation(messageId);
    }

    @PostMapping("/delegate-confirm")
    @ResponseBody
    public Map<String, String> delegateConfirm(@RequestParam String messageId, @RequestParam String assignee,
            @RequestParam String draftBody) {
        flowService.confirmDelegation(messageId, assignee, draftBody);
        return Map.of("status", "success");
    }

    @PostMapping("/delegate-manual")
    public String delegateManual(@RequestParam String messageId, @RequestParam String assignee) {
        flowService.processManualDelegation(messageId, assignee);
        return "redirect:/kanban";
    }

    @PostMapping("/update-mail-tag")
    public String updateMailTag(@RequestParam("messageId") String messageId, @RequestParam("tag") String tag) {
        flowService.updateMailTagById(messageId, tag);
        return "redirect:/";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam String messageId, @RequestParam String status) {
        flowService.updateStatusById(messageId, status.toUpperCase());
        return "redirect:/kanban";
    }

    @PostMapping("/fetch")
    public String fetch() throws Exception {
        flowService.fetchMails();
        return "redirect:/";
    }

    @PostMapping("/analyze")
    public String analyze() {
        flowService.processPendingMails(PersonaResourceService.loadPersona());
        return "redirect:/";
    }

    @PostMapping("/sync")
    public String sync() throws Exception{
        flowService.syncToGmail();
        flowService.fetchMails();
        return "redirect:/";
    }

    @PostMapping("/auto-status")
    public String autoStatus() {
        flowService.detectStatusWithAI();
        return "redirect:/kanban";
    }

    @PostMapping("/persona")
    public String persona(@RequestParam("persona") String p) throws Exception {
        PersonaResourceService.savePersona(Persona.valueOf(p.toUpperCase()));
        return "redirect:/";
    }

    @PostMapping("/events/prepare")
    public ResponseEntity<byte[]> prepareMeeting(@RequestParam String messageId) {
        try {
            String memoText = meetingPrepService.generateMeetingMemo(messageId);
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("====================================="));
            document.add(new Paragraph("        FICHE DE PREPARATION (IA)    "));
            document.add(new Paragraph("=====================================\n\n"));
            document.add(new Paragraph(memoText));
            document.close();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Fiche_Memo_" + messageId + ".pdf");
            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/update-note-tag")
    public String updateNoteTag(@RequestParam("index") int index, @RequestParam("tag") String tag) {
        noteService.updateNoteTag(index, tag);
        return "redirect:/knowledge";
    }

    @PostMapping("/knowledge/delete")
    public String deleteNote(@RequestParam("index") int index) {
        noteService.deleteNote(index);
        return "redirect:/knowledge";
    }

    // =========================================================
    // API REST NIVEAU 3 (HATEOAS)
    // =========================================================

    /**
     * Endpoint racine de l'API (Collection de ressources)
     * Renvoye la liste de tous les mails avec des liens dynamiques
     */
    @GetMapping("/api/mails")
    @ResponseBody
    public RestResponse<List<RestResponse<Mail>>> apiGetAllMails() {
        List<Mail> mails = flowService.getMails();
        if (mails == null) mails = new ArrayList<>();

        List<RestResponse<Mail>> mailResponses = new ArrayList<>();
        
        for (Mail mail : mails) {
            Map<String, String> links = new HashMap<>();
            
            // Liens universels pour cette ressource (Niveau 3)
            links.put("self", "/api/mails/" + mail.getMessageId());
            links.put("update-status", "/update-status?messageId=" + mail.getMessageId());
            
            // Liens contextuels (Le cœur du niveau 3 HATEOAS)
            if (mail.getAction() == EisenhowerAction.DELEGATE) {
                links.put("delegate-auto", "/delegate-auto?messageId=" + mail.getMessageId());
            } else if (mail.getAction() == EisenhowerAction.PLAN) {
                links.put("prepare-meeting", "/events/prepare?messageId=" + mail.getMessageId());
            } else if (mail.getAction() == EisenhowerAction.PENDING) {
                links.put("analyze", "/analyze");
            }

            mailResponses.add(new RestResponse<>(mail, links));
        }

        // Liens globaux pour l'API
        Map<String, String> globalLinks = new HashMap<>();
        globalLinks.put("self", "/api/mails");
        globalLinks.put("fetch", "/fetch");
        globalLinks.put("sync", "/sync");

        return new RestResponse<>(mailResponses, globalLinks);
    }

    /**
     * Endpoint d'une ressource unique
     */
    @GetMapping("/api/mails/{id}")
    @ResponseBody
    public ResponseEntity<RestResponse<Mail>> apiGetMailById(@PathVariable("id") String id) {
        Mail foundMail = flowService.getMails().stream()
                .filter(m -> m.getMessageId().equals(id))
                .findFirst()
                .orElse(null);

        if (foundMail == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/mails/" + id);
        links.put("collection", "/api/mails");
        links.put("update-status", "/update-status?messageId=" + id);

        // HATEOAS : on ne propose la génération de PDF que si le mail est tagué PLAN
        if (foundMail.getAction() == EisenhowerAction.PLAN) {
            links.put("prepare-meeting", "/events/prepare?messageId=" + id);
        }
        if (foundMail.getAction() == EisenhowerAction.DELEGATE) {
            links.put("delegate-auto", "/delegate-auto?messageId=" + id);
        }

        return ResponseEntity.ok(new RestResponse<>(foundMail, links));
    }
}