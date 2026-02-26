package projet.devops.Mail.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import projet.devops.Mail.Mail;
import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Classifier.EisenhowerClassifier;
import projet.devops.Mail.Service.MailFlowService;
import projet.devops.Mail.Service.MeetingPrepService;

record EventItem(String title, String dateLieu, String type, String sourceId) {
}

@Controller
public class EventController {

    private final MailFlowService flowService;
    private final EisenhowerClassifier classifier;
    private final MeetingPrepService meetingPrepService;

    public EventController(MailFlowService flowService, EisenhowerClassifier classifier,
            MeetingPrepService meetingPrepService) {
        this.flowService = flowService;
        this.classifier = classifier;
        this.meetingPrepService = meetingPrepService;
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
        return "events";
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
}