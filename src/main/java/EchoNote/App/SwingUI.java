package EchoNote.App;

import EchoNote.Arpit.ExportService;
import EchoNote.Arpit.SearchService;
import EchoNote.Jack.ActionItem;
import EchoNote.Jack.ExportFormat;
import EchoNote.Jack.ExportResult;
import EchoNote.Jack.MeetingFormatter;
import EchoNote.Jack.MeetingRecord;
import EchoNote.Jack.Summary;
import EchoNote.Jack.Transcript;
import EchoNote.Jack.Workspace;
import EchoNote.Mihail.EmailDraftException;
import EchoNote.Mihail.EmailDraftService;
import EchoNote.Mihail.Recorder;
import EchoNote.Mihail.Summarizer;
import EchoNote.Mihail.Transcriber;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class SwingUI extends JFrame {

    private final Workspace workspace;
    private final Transcriber transcriber;
    private final Summarizer summarizer;
    private final ExportService exportService;
    private final SearchService searchService;
    private final EmailDraftService emailDraftService;
    private final Recorder recorder;

    private final DefaultListModel<MeetingRecord> meetingListModel = new DefaultListModel<>();
    private final JList<MeetingRecord> meetingList = new JList<>(meetingListModel);
    private final JTextArea detailsArea = new JTextArea();
    private final JTextField searchField = new JTextField();
    private final JLabel statusLabel = new JLabel("Ready");

    public SwingUI(AppConfig config) {
        super("EchoNote Demo");

        this.workspace = config.getWorkspace();
        this.transcriber = config.getTranscriber();
        this.summarizer = config.getSummarizer();
        this.exportService = config.getExportService();
        this.searchService = config.getSearchService();
        this.emailDraftService = config.getEmailDraftService();
        this.recorder = new Recorder();

        initLayout();
        initBehavior();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    private void initLayout() {
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(8, 8, 8, 8));

        add(createMeetingListPanel(), BorderLayout.CENTER);
        add(createDetailsPanel(), BorderLayout.EAST);

        statusLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel createMeetingListPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(createSearchPanel(), BorderLayout.NORTH);

        configureMeetingListRenderer();
        meetingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(meetingList), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(new JLabel("Search:"), BorderLayout.WEST);
        panel.add(searchField, BorderLayout.CENTER);

        JButton searchButton = new JButton("Go");
        searchButton.addActionListener(e -> handleSearch());
        panel.add(searchButton, BorderLayout.EAST);

        return panel;
    }

    private void configureMeetingListRenderer() {
        meetingList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MeetingRecord rec) {
                    String title = rec.getTitle() != null ? rec.getTitle() : "(no title)";
                    String date = rec.getDate() != null ? rec.getDate().toString() : "";
                    setText(rec.getId() + " | " + title + " | " + date);
                }
                return this;
            }
        });
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));

        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Details"));
        panel.add(detailsScroll, BorderLayout.CENTER);

        panel.add(createButtonPanel(), BorderLayout.EAST);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));

        addButton(panel, "New Meeting from Mic", e -> handleNewMeetingFromMic());
        addButton(panel, "New Meeting from WAV", e -> handleNewMeetingFromWav());
        addButton(panel, "Export Meeting", e -> handleExportSelected());
        addButton(panel, "Email Summary", e -> handleEmailSelected());
        addButton(panel, "Refresh List", e -> refreshMeetingList());
        addButton(panel, "Clear History", e -> handleClearHistory());
        addButton(panel, "Exit", e -> System.exit(0));

        return panel;
    }

    private void addButton(JPanel panel, String label, java.awt.event.ActionListener action) {
        JButton button = new JButton(label);
        button.addActionListener(action);
        panel.add(button);
    }

    private void initBehavior() {
        meetingList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedMeetingDetails();
            }
        });

        meetingList.addMouseListener(new MouseAdapter() {
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = meetingList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        meetingList.setSelectedIndex(index);
                        MeetingRecord rec = meetingList.getModel().getElementAt(index);
                        showMeetingContextMenu(rec, e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) { maybeShowPopup(e); }

            @Override
            public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
        });

        refreshMeetingList();
    }

    private void handleNewMeetingFromMic() {
        JDialog dialog = new JDialog(this, "Record from Microphone", true);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setSize(350, 230);
        dialog.setLocationRelativeTo(this);

        JPanel center = new JPanel(new GridLayout(0, 1, 4, 4));
        center.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel micLabel = new JLabel("🎙", SwingConstants.CENTER);
        micLabel.setFont(micLabel.getFont().deriveFont(48f));
        micLabel.setForeground(Color.GRAY);

        JLabel instructions = new JLabel("Click Start to begin, Stop to finish.", SwingConstants.CENTER);

        center.add(micLabel);
        center.add(instructions);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton startBtn = new JButton("Start Recording");
        JButton stopBtn = new JButton("Stop Recording");
        JButton cancelBtn = new JButton("Cancel");
        stopBtn.setEnabled(false); // can't stop until started
        bottom.add(startBtn);
        bottom.add(stopBtn);
        bottom.add(cancelBtn);

        dialog.add(center, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);

        startBtn.addActionListener(e -> {
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            cancelBtn.setEnabled(false);
            setStatus("Recording from microphone...");

            try {
                recorder.startInteractiveRecording(
                        "echonote-recording-",
                        level -> SwingUtilities.invokeLater(() -> updateMicLevel(micLabel, level))
                );
            } catch (Exception ex) {
                showError("Could not start recording: " + ex.getMessage());
                dialog.dispose();
            }
        });

        stopBtn.addActionListener(e -> {
            stopBtn.setEnabled(false);
            setStatus("Stopping recording...");

            SwingWorker<Path, Void> worker = new SwingWorker<>() {
                @Override
                protected Path doInBackground() {
                    return recorder.stopInteractiveRecording();
                }

                @Override
                protected void done() {
                    try {
                        Path wavPath = get();
                        dialog.dispose();
                        createMeetingFromWavFile(wavPath.toFile(), true);
                    } catch (Exception ex) {
                        showError("Recording failed: " + ex.getMessage());
                        dialog.dispose();
                    }
                }
            };
            worker.execute();
        });

        cancelBtn.addActionListener(e2 -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void updateMicLevel(JLabel micLabel, double level) {
        double clamped = Math.min(1.0, Math.max(0.0, level));
        int base = 40;
        int red = (int) (base + clamped * (255 - base));
        int green = (int) (base * (1.0 - clamped));
        int blue = (int) (base * (1.0 - clamped));
        micLabel.setForeground(new Color(red, green, blue));
    }

    private void handleNewMeetingFromWav() {
        File recordingsDir = new File("recordings");
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs();
        }

        JFileChooser chooser = new JFileChooser(recordingsDir);
        chooser.setDialogTitle("Choose recorded WAV file");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File wavFile = chooser.getSelectedFile();
        if (!wavFile.exists() || !wavFile.isFile()) {
            showError("File not found: " + wavFile.getAbsolutePath());
            return;
        }

        createMeetingFromWavFile(wavFile, false);
    }

    private void createMeetingFromWavFile(File wavFile, boolean renameBasedOnTitle) {
        try {
            setStatus("Transcribing audio...");
            Transcript transcript = transcriber.transcribeFile(wavFile.toPath());

            setStatus("Generating summary...");
            Summary summary = summarizer.summarize(transcript);

            setStatus("Extracting action items...");
            List<ActionItem> actions = summarizer.extractActions(transcript);

            String title = JOptionPane.showInputDialog(
                    this,
                    "Enter a name for this meeting:",
                    "New Meeting",
                    JOptionPane.PLAIN_MESSAGE
            );
            if (title == null || title.isBlank()) {
                title = "Untitled Meeting";
            }

            File finalWavFile = wavFile;
            if (renameBasedOnTitle) {
                finalWavFile = renameWavToTitle(wavFile, title);
            }

            MeetingRecord record = buildMeetingRecord(transcript, summary, actions, title, finalWavFile);
            workspace.save(record);
            searchService.index(record);

            refreshMeetingList();
            selectMeeting(record);
            setStatus("Meeting created and saved with ID " + record.getId());
        } catch (Exception ex) {
            showError("Error creating meeting: " + ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    private File renameWavToTitle(File original, String title) {
        try {
            File recordingsDir = new File("recordings");
            if (!recordingsDir.exists()) {
                recordingsDir.mkdirs();
            }

            String safe = title.trim().replaceAll("[^a-zA-Z0-9-_ ]", "");
            if (safe.isBlank()) {
                safe = "meeting";
            }
            safe = safe.replace(' ', '_');

            File target = new File(recordingsDir, safe + ".wav");
            int counter = 1;
            while (target.exists()) {
                target = new File(recordingsDir, safe + "-" + counter + ".wav");
                counter++;
            }

            boolean ok = original.renameTo(target);
            if (!ok) {
                return original;
            }
            return target;
        } catch (Exception e) {
            return original;
        }
    }

    private void handleExportSelected() {
        MeetingRecord record = meetingList.getSelectedValue();
        if (record == null) {
            showError("Select a meeting first.");
            return;
        }
        exportMeeting(record);
    }

    private void exportMeeting(MeetingRecord record) {
        // Show format selection dialog
        ExportFormat[] formats = exportService.getSupportedFormats();
        ExportFormat selectedFormat = (ExportFormat) JOptionPane.showInputDialog(
                this,
                "Select export format:",
                "Export Meeting",
                JOptionPane.QUESTION_MESSAGE,
                null,
                formats,
                ExportFormat.MARKDOWN
        );

        if (selectedFormat == null) {
            return; // User cancelled
        }

        ExportResult result = exportService.export(record, selectedFormat);
        if (result.isSuccess()) {
            setStatus("Exported to " + result.getLink());
            JOptionPane.showMessageDialog(this,
                    "Exported to:\n" + result.getLink(),
                    "Export successful",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            showError("Export failed: " + result.getMessage());
        }
    }

    private void handleEmailSelected() {
        MeetingRecord record = meetingList.getSelectedValue();
        if (record == null) {
            showError("Select a meeting first.");
            return;
        }

        // Prompt user for recipient email
        String recipientEmail = JOptionPane.showInputDialog(
                this,
                "Enter recipient email address:",
                "Email Meeting Summary",
                JOptionPane.QUESTION_MESSAGE
        );

        if (recipientEmail == null || recipientEmail.isBlank()) {
            return; // User cancelled or empty input
        }

        try {
            emailDraftService.openEmailDraft(record, recipientEmail.trim());
            setStatus("Email draft opened in default mail client.");
        } catch (EmailDraftException ex) {
            showError("Failed to open email draft: " + ex.getMessage());
        }
    }

    private void handleSearch() {
        String query = searchField.getText().trim();
        List<MeetingRecord> results = searchService.search(query);
        meetingListModel.clear();
        for (MeetingRecord rec : results) {
            meetingListModel.addElement(rec);
        }
        setStatus("Found " + results.size() + " meeting(s).");
    }

    private void showMeetingContextMenu(MeetingRecord record, int x, int y) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem exportItem = new JMenuItem("Export as Markdown");
        JMenuItem openExportItem = new JMenuItem("Open Export Location");
        JMenuItem openWavItem = new JMenuItem("Open WAV Location");

        exportItem.addActionListener(e -> exportMeeting(record));
        openExportItem.addActionListener(e -> openExportLocation(record));
        openWavItem.addActionListener(e -> openWavLocation(record));

        menu.add(exportItem);
        menu.add(openExportItem);
        menu.add(openWavItem);

        menu.show(meetingList, x, y);
    }

    private void openExportLocation(MeetingRecord record) {
        try {
            File dir = exportService.getExportDirectory();
            if (dir == null) {
                showError("Export directory is not configured.");
                return;
            }

            File file = new File(dir, "meeting-" + record.getId() + ".md");
            File toOpen = file.exists() ? file.getParentFile() : dir;

            if (!toOpen.exists()) {
                showError("Export directory does not exist yet. Export a meeting first.");
                return;
            }

            if (!Desktop.isDesktopSupported()) {
                showError("Desktop operations are not supported on this platform.");
                return;
            }

            Desktop.getDesktop().open(toOpen);
            setStatus("Opened location: " + toOpen.getAbsolutePath());
        } catch (Exception ex) {
            showError("Could not open file location: " + ex.getMessage());
        }
    }

    private void openWavLocation(MeetingRecord record) {
        try {
            String path = record.getAudioFilePath();
            if (path == null || path.isBlank()) {
                showError("No audio file associated with this meeting.");
                return;
            }

            File file = new File(path);
            if (!file.exists()) {
                showError("Audio file not found:\n" + path);
                return;
            }

            File toOpen = file.getParentFile() != null ? file.getParentFile() : file;
            if (!Desktop.isDesktopSupported()) {
                showError("Desktop operations are not supported on this platform.");
                return;
            }

            Desktop.getDesktop().open(toOpen);
            setStatus("Opened audio location: " + toOpen.getAbsolutePath());
        } catch (Exception ex) {
            showError("Could not open WAV location: " + ex.getMessage());
        }
    }

    private MeetingRecord buildMeetingRecord(Transcript transcript,
                                             Summary summary,
                                             List<ActionItem> actions,
                                             String title,
                                             File wavFile) {
        MeetingRecord record = new MeetingRecord();
        record.setTitle(title);
        record.setDate(LocalDateTime.now());
        record.setTranscript(transcript);
        record.setSummary(summary);
        record.setActions(actions);
        if (wavFile != null) {
            record.setAudioFilePath(wavFile.getAbsolutePath());
        }
        return record;
    }

    private void handleClearHistory() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear all meeting history?\nThis action cannot be undone.",
                "Clear History",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            boolean cleared = workspace.clearAll();
            if (cleared) {
                searchService.clearIndex();
                refreshMeetingList();
                detailsArea.setText("");
                setStatus("Meeting history cleared successfully.");
            } else {
                showError("Failed to clear meeting history.");
            }
        }
    }

    private void refreshMeetingList() {
        meetingListModel.clear();
        List<MeetingRecord> all = searchService.search("");
        for (MeetingRecord rec : all) {
            meetingListModel.addElement(rec);
        }
    }

    private void selectMeeting(MeetingRecord record) {
        meetingList.setSelectedValue(record, true);
    }

    private void showSelectedMeetingDetails() {
        MeetingRecord record = meetingList.getSelectedValue();
        if (record == null) {
            detailsArea.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(record.getId()).append("\n");
        sb.append("Title: ").append(record.getTitle()).append("\n");
        sb.append("Date: ").append(record.getDate()).append("\n\n");

        if (record.getSummary() != null) {
            sb.append("SUMMARY\n");
            sb.append(MeetingFormatter.formatSummaryAsText(record.getSummary()));
        }

        if (!record.getActions().isEmpty()) {
            sb.append("ACTION ITEMS\n");
            sb.append(MeetingFormatter.formatActionsAsText(record.getActions()));
        }

        if (record.getAudioFilePath() != null) {
            sb.append("\nAudio file: ").append(record.getAudioFilePath()).append("\n");
        }

        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    private void showError(String msg) {
        statusLabel.setText("Error: " + msg);
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
