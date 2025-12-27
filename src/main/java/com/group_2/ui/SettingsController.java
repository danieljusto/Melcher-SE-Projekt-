package com.group_2.ui;

import com.group_2.service.RoomService;
import com.group_2.service.WGService;
import com.group_2.util.SessionManager;
import com.model.Room;
import com.model.User;
import com.model.WG;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Controller for the WG settings view.
 * Shows rooms, members, invitation code, and admin controls.
 */
@Component
public class SettingsController extends Controller {

    private final SessionManager sessionManager;
    private final WGService wgService;
    private final RoomService roomService;
    private final com.group_2.service.CleaningScheduleService cleaningScheduleService;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private Text wgNameHeader;
    @FXML
    private Text inviteCodeText;
    @FXML
    private Text roomCountText;
    @FXML
    private Text memberCountText;
    @FXML
    private VBox roomsBox;
    @FXML
    private VBox membersBox;
    @FXML
    private VBox adminCard;
    @FXML
    private javafx.scene.control.Button addRoomButton;

    @Autowired
    public SettingsController(SessionManager sessionManager, WGService wgService, RoomService roomService,
            com.group_2.service.CleaningScheduleService cleaningScheduleService) {
        this.sessionManager = sessionManager;
        this.wgService = wgService;
        this.roomService = roomService;
        this.cleaningScheduleService = cleaningScheduleService;
    }

    public void initView() {
        sessionManager.refreshCurrentUser();
        loadWGData();
    }

    private void loadWGData() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null)
            return;

        WG wg = currentUser.getWg();
        if (wg == null) {
            showAlert(Alert.AlertType.WARNING, "No WG", "You are not a member of any WG.",
                    getOwnerWindow(wgNameHeader));
            return;
        }

        // Header
        wgNameHeader.setText(wg.name);
        inviteCodeText.setText(wg.getInviteCode());

        // Check if admin
        boolean isAdmin = wg.admin != null && wg.admin.getId().equals(currentUser.getId());
        adminCard.setVisible(isAdmin);
        adminCard.setManaged(isAdmin);

        // Show/hide Add Room button based on admin status
        if (addRoomButton != null) {
            addRoomButton.setVisible(isAdmin);
            addRoomButton.setManaged(isAdmin);
        }

        // Load rooms
        loadRooms(wg);

        // Load members
        loadMembers(wg);
    }

    private void loadRooms(WG wg) {
        roomsBox.getChildren().clear();
        User currentUser = sessionManager.getCurrentUser();
        boolean isAdmin = wg.admin != null && currentUser != null && wg.admin.getId().equals(currentUser.getId());

        if (wg.rooms != null && !wg.rooms.isEmpty()) {
            roomCountText.setText(wg.rooms.size() + " room" + (wg.rooms.size() > 1 ? "s" : "") + " in your WG");
            for (Room room : wg.rooms) {
                roomsBox.getChildren().add(createRoomListItem(room, isAdmin));
            }
        } else {
            roomCountText.setText("No rooms yet");
            Text noRooms = new Text(isAdmin ? "Add your first room above!" : "No rooms have been added yet.");
            noRooms.setStyle("-fx-fill: #64748b;");
            roomsBox.getChildren().add(noRooms);
        }
    }

    private void loadMembers(WG wg) {
        membersBox.getChildren().clear();
        if (wg.mitbewohner != null && !wg.mitbewohner.isEmpty()) {
            memberCountText.setText(
                    wg.mitbewohner.size() + " member" + (wg.mitbewohner.size() > 1 ? "s" : "") + " in your WG");
            for (User user : wg.mitbewohner) {
                membersBox.getChildren().add(createMemberListItem(user, wg));
            }
        } else {
            memberCountText.setText("No members yet");
        }
    }

    private HBox createRoomListItem(Room room, boolean isAdmin) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("list-item");
        item.setPadding(new Insets(10, 15, 10, 15));

        StackPane iconPane = new StackPane();
        iconPane.getStyleClass().add("avatar");
        iconPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #10b981, #059669);");
        Text iconText = new Text("🚪");
        iconText.setStyle("-fx-font-size: 18px;");
        iconPane.getChildren().add(iconText);

        VBox info = new VBox(3);
        javafx.scene.layout.HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
        Text nameText = new Text(room.getName());
        nameText.getStyleClass().add("list-item-title");
        Text idText = new Text("Room ID: " + room.getId());
        idText.getStyleClass().add("list-item-subtitle");
        info.getChildren().addAll(nameText, idText);

        item.getChildren().addAll(iconPane, info);

        // Admin actions for rooms
        if (isAdmin) {
            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER_RIGHT);

            javafx.scene.control.Button editBtn = new javafx.scene.control.Button("✏️");
            editBtn.setTooltip(new javafx.scene.control.Tooltip("Edit Room"));
            editBtn.setStyle(
                    "-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; -fx-background-radius: 6; -fx-padding: 6 10; -fx-cursor: hand;");
            editBtn.setOnAction(e -> editRoom(room));
            actions.getChildren().add(editBtn);

            javafx.scene.control.Button deleteBtn = new javafx.scene.control.Button("🗑️");
            deleteBtn.setTooltip(new javafx.scene.control.Tooltip("Delete Room"));
            deleteBtn.setStyle(
                    "-fx-background-color: #fef2f2; -fx-text-fill: #dc2626; -fx-background-radius: 6; -fx-padding: 6 10; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> deleteRoom(room));
            actions.getChildren().add(deleteBtn);

            item.getChildren().add(actions);
        }

        return item;
    }

    private HBox createMemberListItem(User user, WG wg) {
        User currentUser = sessionManager.getCurrentUser();
        boolean currentUserIsAdmin = wg.admin != null && wg.admin.getId().equals(currentUser.getId());
        boolean isMemberAdmin = wg.admin != null && wg.admin.getId().equals(user.getId());
        boolean isSelf = user.getId().equals(currentUser.getId());

        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("list-item");
        item.setPadding(new Insets(10, 15, 10, 15));

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("avatar");
        String initial = user.getName() != null && !user.getName().isEmpty()
                ? user.getName().substring(0, 1).toUpperCase()
                : "?";
        Text avatarText = new Text(initial);
        avatarText.getStyleClass().add("avatar-text");
        avatar.getChildren().add(avatarText);

        VBox info = new VBox(3);
        javafx.scene.layout.HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
        String fullName = user.getName() + (user.getSurname() != null ? " " + user.getSurname() : "");
        Text nameText = new Text(fullName + (isMemberAdmin ? " 👑" : "") + (isSelf ? " (You)" : ""));
        nameText.getStyleClass().add("list-item-title");
        Text emailText = new Text(user.getEmail() != null ? user.getEmail() : "No email");
        emailText.getStyleClass().add("list-item-subtitle");
        info.getChildren().addAll(nameText, emailText);

        item.getChildren().addAll(avatar, info);

        // Admin actions (only show if current user is admin and not viewing themselves)
        if (currentUserIsAdmin && !isSelf) {
            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER_RIGHT);

            if (!isMemberAdmin) {
                // Make Admin button
                javafx.scene.control.Button makeAdminBtn = new javafx.scene.control.Button("👑");
                makeAdminBtn.setTooltip(new javafx.scene.control.Tooltip("Make Admin"));
                makeAdminBtn.setStyle(
                        "-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-background-radius: 6; -fx-padding: 6 10; -fx-cursor: hand;");
                makeAdminBtn.setOnAction(e -> handleMakeAdmin(user));
                actions.getChildren().add(makeAdminBtn);
            }

            // Remove button
            javafx.scene.control.Button removeBtn = new javafx.scene.control.Button("✕");
            removeBtn.setTooltip(new javafx.scene.control.Tooltip("Remove from WG"));
            removeBtn.setStyle(
                    "-fx-background-color: #fef2f2; -fx-text-fill: #dc2626; -fx-background-radius: 6; -fx-padding: 6 10; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> handleRemoveMember(user));
            actions.getChildren().add(removeBtn);

            item.getChildren().add(actions);
        }

        return item;
    }

    private void handleMakeAdmin(User user) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getWg() == null)
            return;

        WG wg = currentUser.getWg();
        String userName = user.getName() + (user.getSurname() != null ? " " + user.getSurname() : "");

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        configureDialogOwner(confirmDialog, getOwnerWindow(membersBox));
        confirmDialog.setTitle("Transfer Admin Rights");
        confirmDialog.setHeaderText("Make " + userName + " the new admin?");
        confirmDialog.setContentText("You will no longer be the admin of this WG.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                wgService.updateWG(wg.getId(), wg.name, user);
                sessionManager.refreshCurrentUser();
                loadWGData();
                showAlert(Alert.AlertType.INFORMATION, "Success", userName + " is now the admin!",
                        getOwnerWindow(membersBox));
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to transfer admin rights: " + e.getMessage(),
                        getOwnerWindow(membersBox));
            }
        }
    }

    private void handleRemoveMember(User user) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getWg() == null)
            return;

        WG wg = currentUser.getWg();
        String userName = user.getName() + (user.getSurname() != null ? " " + user.getSurname() : "");

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        configureDialogOwner(confirmDialog, getOwnerWindow(membersBox));
        confirmDialog.setTitle("Remove Member");
        confirmDialog.setHeaderText("Remove " + userName + " from the WG?");
        confirmDialog.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                wgService.removeMitbewohner(wg.getId(), user.getId());
                sessionManager.refreshCurrentUser();
                loadWGData();
                showAlert(Alert.AlertType.INFORMATION, "Success", userName + " has been removed from the WG.",
                        getOwnerWindow(membersBox));
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove member: " + e.getMessage(),
                        getOwnerWindow(membersBox));
            }
        }
    }

    @FXML
    public void copyInviteCode() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getWg() != null) {
            String code = currentUser.getWg().getInviteCode();
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(code);
            clipboard.setContent(content);
            showAlert(Alert.AlertType.INFORMATION, "Copied!", "Invitation code copied to clipboard: " + code,
                    getOwnerWindow(inviteCodeText));
        }
    }

    @FXML
    public void addRoom() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getWg() == null)
            return;

        // Only admin can add rooms
        WG wg = currentUser.getWg();
        if (wg.admin == null || !wg.admin.getId().equals(currentUser.getId())) {
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "Only the admin can add rooms.",
                    getOwnerWindow(roomsBox));
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        configureDialogOwner(dialog, getOwnerWindow(roomsBox));
        dialog.setTitle("Add Room");
        dialog.setHeaderText("Add a new room to your WG");
        dialog.setContentText("Room name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(roomName -> {
            if (!roomName.trim().isEmpty()) {
                try {
                    Room newRoom = roomService.createRoom(roomName.trim());
                    wgService.addRoom(currentUser.getWg().getId(), newRoom);
                    sessionManager.refreshCurrentUser();
                    loadWGData();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Room '" + roomName + "' added!",
                            getOwnerWindow(roomsBox));
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add room: " + e.getMessage(),
                            getOwnerWindow(roomsBox));
                }
            }
        });
    }

    private void editRoom(Room room) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getWg() == null)
            return;

        TextInputDialog dialog = new TextInputDialog(room.getName());
        configureDialogOwner(dialog, getOwnerWindow(roomsBox));
        dialog.setTitle("Edit Room");
        dialog.setHeaderText("Edit room name");
        dialog.setContentText("Room name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                try {
                    roomService.updateRoom(room.getId(), newName.trim());
                    sessionManager.refreshCurrentUser();
                    loadWGData();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Room renamed to '" + newName + "'!",
                            getOwnerWindow(roomsBox));
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to rename room: " + e.getMessage(),
                            getOwnerWindow(roomsBox));
                }
            }
        });
    }

    private void deleteRoom(Room room) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getWg() == null)
            return;

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        configureDialogOwner(confirmDialog, getOwnerWindow(roomsBox));
        confirmDialog.setTitle("Delete Room");
        confirmDialog.setHeaderText("Delete room '" + room.getName() + "'?");
        confirmDialog
                .setContentText("This will also remove any cleaning tasks and templates associated with this room.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // First delete all cleaning-related data for this room
                cleaningScheduleService.deleteRoomData(room);
                // Then remove the room from the WG
                wgService.removeRoom(currentUser.getWg().getId(), room);
                // Finally delete the room itself
                roomService.deleteRoom(room.getId());
                sessionManager.refreshCurrentUser();
                loadWGData();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Room '" + room.getName() + "' deleted!",
                        getOwnerWindow(roomsBox));
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete room: " + e.getMessage(),
                        getOwnerWindow(roomsBox));
            }
        }
    }

    @FXML
    public void editWgName() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getWg() == null)
            return;

        TextInputDialog dialog = new TextInputDialog(currentUser.getWg().name);
        configureDialogOwner(dialog, getOwnerWindow(wgNameHeader));
        dialog.setTitle("Edit WG Name");
        dialog.setHeaderText("Change your WG name");
        dialog.setContentText("New name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                try {
                    WG wg = currentUser.getWg();
                    wgService.updateWG(wg.getId(), newName.trim(), wg.admin);
                    sessionManager.refreshCurrentUser();
                    loadWGData();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "WG name updated!", getOwnerWindow(wgNameHeader));
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update WG name: " + e.getMessage(),
                            getOwnerWindow(wgNameHeader));
                }
            }
        });
    }

    @FXML
    public void deleteWG() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getWg() == null)
            return;

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        configureDialogOwner(confirmDialog, getOwnerWindow(wgNameHeader));
        confirmDialog.setTitle("Delete WG");
        confirmDialog.setHeaderText("Are you sure you want to delete this WG?");
        confirmDialog.setContentText("This action cannot be undone. All members will be removed.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Long wgId = currentUser.getWg().getId();
                wgService.deleteWG(wgId);
                sessionManager.refreshCurrentUser();
                showAlert(Alert.AlertType.INFORMATION, "Success", "WG deleted.", getOwnerWindow(wgNameHeader));
                loadScene(wgNameHeader.getScene(), "/no_wg.fxml");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete WG: " + e.getMessage(),
                        getOwnerWindow(wgNameHeader));
            }
        }
    }

    @FXML
    public void backToHome() {
        loadScene(wgNameHeader.getScene(), "/main_screen.fxml");
        javafx.application.Platform.runLater(() -> {
            MainScreenController mainController = applicationContext.getBean(MainScreenController.class);
            mainController.initView();
        });
    }
}
