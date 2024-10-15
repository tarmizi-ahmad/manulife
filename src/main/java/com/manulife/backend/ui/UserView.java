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

import java.io.FileOutputStream;
import java.io.IOException;
import net.sf.jasperreports.engine.JRException;

@Route("users") // Accessible at http://localhost:8080/users
public class UserView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> userGrid = new Grid<>(User.class);
    private final TextField filterText = new TextField();

    @Autowired
    public UserView(UserService userService) {
        this.userService = userService;

        // Configure Grid with User data
        userGrid.setColumns("id", "firstName", "lastName", "email");
        ListDataProvider<User> dataProvider = new ListDataProvider<>(userService.getAllUsers());
        userGrid.setDataProvider(dataProvider);
        userGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Adding Update and Delete buttons to each row
        userGrid.addComponentColumn(user -> {
            Button updateButton = new Button("Update", e -> openUpdateUserDialog(user));
            Button deleteButton = new Button("Delete", e -> confirmDeleteUser(user));
            HorizontalLayout actionLayout = new HorizontalLayout(updateButton, deleteButton);
            return actionLayout;
        }).setHeader("Actions");

        // Create buttons
        Button addButton = new Button("Add User", e -> openAddUserDialog());
        Button reportButton = new Button("Generate Report", e -> generateReport());

        // Add a filter text field
        filterText.setPlaceholder("Filter by name or email...");
        filterText.addValueChangeListener(event -> updateList(dataProvider));
        
        // Add buttons and filter field above the grid
        HorizontalLayout buttonLayout = new HorizontalLayout(addButton, reportButton);
        add(filterText, buttonLayout, userGrid);
    }

    private void updateList(ListDataProvider<User> dataProvider) {
        String filter = filterText.getValue();
        if (filter == null || filter.isEmpty()) {
            dataProvider.setFilter(null);
        } else {
            dataProvider.setFilter(user -> 
                user.getFirstName().toLowerCase().contains(filter.toLowerCase()) || 
                user.getLastName().toLowerCase().contains(filter.toLowerCase()) ||
                user.getEmail().toLowerCase().contains(filter.toLowerCase()));
        }
    }

    private void openAddUserDialog() {
        Dialog dialog = new Dialog();
        dialog.add(createUserForm(null, dialog));
        dialog.open();
    }

    private void openUpdateUserDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.add(createUserForm(user, dialog));
        dialog.open();
    }

    private VerticalLayout createUserForm(User user, Dialog dialog) {
        TextField firstNameField = new TextField("First Name");
        TextField lastNameField = new TextField("Last Name");
        EmailField emailField = new EmailField("Email");

        if (user != null) {
            firstNameField.setValue(user.getFirstName());
            lastNameField.setValue(user.getLastName());
            emailField.setValue(user.getEmail());
        }

        Button saveButton = new Button(user == null ? "Add User" : "Update User", e -> {
            String firstName = firstNameField.getValue();
            String lastName = lastNameField.getValue();
            String email = emailField.getValue();

            if (validateUserInput(firstName, lastName, email)) {
                if (user == null) {
                    User newUser = new User();
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setEmail(email);
                    userService.saveUser(newUser);
                    Notification.show("User added!", 3000, Notification.Position.TOP_CENTER);
                } else {
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setEmail(email);
                    userService.updateUser(user.getId(), user);
                    Notification.show("User updated!", 3000, Notification.Position.TOP_CENTER);
                }
                refreshGrid();
                dialog.close();
            }
        });

        VerticalLayout formLayout = new VerticalLayout(firstNameField, lastNameField, emailField, saveButton);
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
        if (email == null || email.isEmpty()) {
            Notification.show("Email is required.", 3000, Notification.Position.TOP_CENTER);
            return false;
        }
        if (!email.contains("@")) {
            Notification.show("Please enter a valid email address.", 3000, Notification.Position.TOP_CENTER);
            return false;
        }
        return true;
    }

    private void refreshGrid() {
        userGrid.setItems(userService.getAllUsers());
    }

    private void generateReport() {
        try {
            byte[] pdfContent = userService.generateUserReport();

            try (FileOutputStream out = new FileOutputStream("user_report.pdf")) {
                out.write(pdfContent);
            }

            Notification.show("Report generated successfully!", 3000, Notification.Position.TOP_CENTER);

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

        Button cancelButton = new Button("Cancel", e -> confirmation.close());

        confirmation.add(deleteButton, cancelButton);
        confirmation.open();
    }
}
