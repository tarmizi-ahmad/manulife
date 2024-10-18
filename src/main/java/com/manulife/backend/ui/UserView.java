package com.manulife.backend.ui;

import com.manulife.backend.model.User;
import com.manulife.backend.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.jasperreports.engine.JRException;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;

@Route("users") // Accessible at http://localhost:8080/users
public class UserView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> userGrid = new Grid<>(User.class);
    private final TextField filterText = new TextField();
    private final ListDataProvider<User> dataProvider;

    private VerticalLayout downloadLayout = new VerticalLayout();

    @Autowired
    public UserView(UserService userService) {
        this.userService = userService;
        this.dataProvider = new ListDataProvider<>(userService.getAllUsers());

        // Configure Grid with User data
        userGrid.setColumns("id", "firstName", "lastName", "email");
        userGrid.setDataProvider(dataProvider);
        userGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Adding Update and Delete buttons to each row
        userGrid.addComponentColumn(user -> {
            Button updateButton = new Button("Update", e -> openUserDialog(user));
            Button deleteButton = new Button("Delete", e -> confirmDeleteUser(user));
            HorizontalLayout actionLayout = new HorizontalLayout(updateButton, deleteButton);
            return actionLayout;
        }).setHeader("Actions");

        // Create buttons
        Button addButton = new Button("Add User", e -> openUserDialog(null));
        addButton.getElement().getStyle().set("cursor", "pointer");

        Button reportButton = new Button("Generate Report", e -> generateReport());
        reportButton.getElement().getStyle().set("cursor", "pointer");

        // Configure filter text field with a clear button
        filterText.setPlaceholder("Filter by name or email...");
        filterText.setClearButtonVisible(true);
        filterText.addValueChangeListener(event -> updateList());

        // Layout for buttons and filter text field
        HorizontalLayout toolbarLayout = new HorizontalLayout(filterText, addButton, reportButton);
        toolbarLayout.setWidthFull();
        toolbarLayout.setAlignItems(Alignment.CENTER);

        // Add toolbar and grid to the main layout
        add(toolbarLayout, userGrid);
    }

    private void updateList() {
        String filter = filterText.getValue().trim().toLowerCase();
    
        // Apply filter directly and update the grid's items
        userGrid.setItems(userService.getAllUsers().stream()
            .filter(user ->
                user.getFirstName().toLowerCase().contains(filter) ||
                user.getLastName().toLowerCase().contains(filter) ||
                user.getEmail().toLowerCase().contains(filter))
            .toList()
        );
    }

    private void openUserDialog(User user) {
        Dialog dialog = new Dialog();

        // Set dialog title based on action
        String title = (user == null) ? "Add User" : "Update User";
        dialog.setHeaderTitle(title);

        VerticalLayout formLayout = createUserForm(user, dialog);
        dialog.add(formLayout);

        // Add an 'X' icon button at the top-right corner for closing the dialog
        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE), event -> dialog.close());
        closeButton.getElement().setAttribute("theme", "tertiary icon"); // Styling for icon-only button
        closeButton.getElement().getStyle().set("cursor", "pointer");

        // Add the close button to the dialog header
        dialog.getHeader().add(closeButton);

        dialog.open();
    }

    private VerticalLayout createUserForm(User user, Dialog dialog) {
        TextField firstNameField = new TextField("First Name");
        TextField lastNameField = new TextField("Last Name");
        EmailField emailField = new EmailField("Email");

        // Pre-fill the form if updating an existing user
        if (user != null) {
            firstNameField.setValue(user.getFirstName());
            lastNameField.setValue(user.getLastName());
            emailField.setValue(user.getEmail());
        }

        // Save button to handle both Add and Update actions
        Button saveButton = new Button(user == null ? "Add User" : "Update User", e -> {
            String firstName = firstNameField.getValue();
            String lastName = lastNameField.getValue();
            String email = emailField.getValue();

            if (validateUserInput(firstName, lastName, email)) {
                if (user == null) {  // Add new user
                    User newUser = new User();
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setEmail(email);
                    userService.saveUser(newUser);
                    Notification.show("User added!", 3000, Notification.Position.TOP_CENTER);
                } else {  // Update existing user
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setEmail(email);
                    userService.updateUser(user.getId(), user);
                    Notification.show("User updated!", 3000, Notification.Position.TOP_CENTER);
                }
                refreshGrid();
                dialog.close();  // Close dialog after saving
            }
        });
        saveButton.getElement().getStyle().set("cursor", "pointer");

        // Cancel button to close the dialog without saving
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.getElement().getStyle().set("cursor", "pointer");

        // Layout for Save and Cancel buttons
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        // Add all components to the form layout
        VerticalLayout formLayout = new VerticalLayout(
            firstNameField, lastNameField, emailField, buttonLayout
        );
        formLayout.setPadding(true);
        return formLayout;
    }

    private boolean validateUserInput(String firstName, String lastName, String email) {
        if (firstName == null || firstName.isEmpty()) {
            Notification.show("First Name is required.", 3000, Notification.Position.TOP_CENTER);
            return false;
        }
        if (lastName == null || lastName.isEmpty()) {
            Notification.show("Last Name is required.", 3000, Notification.Position.TOP_CENTER);
            return false;
        }
        if (email == null || email.isEmpty() || !email.contains("@")) {
            Notification.show("Valid Email is required.", 3000, Notification.Position.TOP_CENTER);
            return false;
        }
        return true;
    }

    private void refreshGrid() {
        userGrid.setItems(userService.getAllUsers());
    }

    private void generateReport() {
        try {
            // 1. Generate PDF content
            byte[] pdfContent = userService.generateUserReport();

            // 2. Save PDF to resources/reports directory
            String filePath = "src/main/resources/reports/user_report.pdf";
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                out.write(pdfContent);
            }

            // 3. Create StreamResource for downloading the PDF
            StreamResource resource = new StreamResource("user_report.pdf",
                () -> new ByteArrayInputStream(pdfContent));

            // 4. Create Anchor with download link
            Anchor downloadLink = new Anchor(resource, "Click here to download the report");
            downloadLink.getElement().setAttribute("download", true);
            
            // Clear previous download links before adding new one
            downloadLayout.removeAll();
            downloadLayout.add(downloadLink); // Add the new link to the layout

            // 5. Show notification
            Notification.show("Report generated successfully!", 3000, Notification.Position.TOP_CENTER);
            
            // Add the download layout to the main layout (if not already added)
            if (!this.getChildren().anyMatch(component -> component == downloadLayout)) {
                add(downloadLayout);
            }

        } catch (JRException | IOException e) {
            Notification.show("Error generating report: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER);
        }
    }

    private void confirmDeleteUser(User user) {
        Notification confirmation = new Notification(
            "Are you sure you want to delete this user?",
            3000,
            Notification.Position.TOP_CENTER
        );

        Button deleteButton = new Button("Delete", e -> {
            userService.deleteUser(user.getId());
            refreshGrid();
            confirmation.close();
            Notification.show("User deleted!", 3000, Notification.Position.TOP_CENTER);
        });
        deleteButton.getElement().getStyle().set("cursor", "pointer");

        Button cancelButton = new Button("Cancel", e -> confirmation.close());
        cancelButton.getElement().getStyle().set("cursor", "pointer");

        confirmation.add(deleteButton, cancelButton);
        confirmation.open();
    }
}
