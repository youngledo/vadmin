package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import io.github.vaadinadminstarter.app.customer.Customer;
import io.github.vaadinadminstarter.app.customer.CustomerAttachment;
import io.github.vaadinadminstarter.app.customer.CustomerService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.contracts.error.BusinessFailure;
import io.github.vaadinadminstarter.contracts.file.FileStorage;
import io.github.vaadinadminstarter.flow.patterns.DataWorkspace;
import io.github.vaadinadminstarter.flow.patterns.ConfirmationDialog;
import io.github.vaadinadminstarter.flow.patterns.DetailDialog;
import io.github.vaadinadminstarter.flow.patterns.EditorDialog;
import io.github.vaadinadminstarter.flow.patterns.FlowFileUpload;
import io.github.vaadinadminstarter.flow.patterns.OperationFeedback;
import io.github.vaadinadminstarter.flow.patterns.PagedGrid;
import io.github.vaadinadminstarter.flow.patterns.PageHeader;
import io.github.vaadinadminstarter.flow.patterns.PageToolbar;
import io.github.vaadinadminstarter.flow.navigation.PermissionProtectedView;
import jakarta.annotation.security.PermitAll;
import java.io.ByteArrayInputStream;
import java.util.Map;

@PageTitle("Customers")
@PermitAll
@org.springframework.stereotype.Component
@org.springframework.context.annotation.Scope(org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class CustomersView extends PermissionProtectedView {
    public static final PermissionCode REQUIRED_PERMISSION = PermissionCode.of("customer:customer:read");
    private static final PermissionCode CREATE = PermissionCode.of("customer:customer:create");
    private static final PermissionCode UPDATE = PermissionCode.of("customer:customer:update");
    private static final PermissionCode DELETE = PermissionCode.of("customer:customer:delete");
    private static final PermissionCode ATTACHMENT_UPLOAD = PermissionCode.of("customer:attachment:upload");

    private final CustomerService customers;
    private final FlowFileUpload fileUpload;
    private final Grid<Customer> grid = new Grid<>(Customer.class, false);
    private final TextField filter = new TextField();
    private final PagedGrid<Customer> pages;
    private final OperationFeedback feedback = new OperationFeedback();

    public CustomersView(CurrentUserProvider currentUser, AuthorizationService authorization,
                         CustomerService customers, FileStorage fileStorage) {
        super(currentUser, authorization);
        this.customers = customers;
        this.fileUpload = new FlowFileUpload(authorization, fileStorage);
        filter.setLabel(getTranslation("customers.filter"));
        filter.setPlaceholder(getTranslation("customers.filter-placeholder"));
        filter.setClearButtonVisible(true);
        filter.setValueChangeMode(ValueChangeMode.EAGER);

        grid.addColumn(Customer::name).setHeader(getTranslation("customers.name")).setAutoWidth(true);
        grid.addColumn(Customer::email).setHeader(getTranslation("customers.email")).setAutoWidth(true);
        grid.addColumn(customer -> customer.active() ? getTranslation("customers.enabled") : getTranslation("customers.disabled")).setHeader(getTranslation("customers.status"));
        grid.addComponentColumn(customer -> actions(customer, authorization)).setHeader(getTranslation("customers.actions")).setAutoWidth(true);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        pages = new PagedGrid<>(grid, query -> customers.page(requireCurrentUser(), query),
                () -> Map.of("q", filter.getValue()), "name");
        filter.addValueChangeListener(event -> pages.refresh());

        var header = PageHeader.translated("customers.customers.title", "customers.customers.intent");
        var toolbar = new PageToolbar();
        toolbar.getElement().setAttribute("data-testid", "customers-toolbar");
        toolbar.addFilter(filter);
        var create = new Button(getTranslation("customers.create"), VaadinIcon.PLUS.create(), event -> edit(null));
        create.setVisible(authorization.hasPermission(requireCurrentUser(), CREATE));
        toolbar.setPrimaryAction(create);
        var workspace = new DataWorkspace<>(grid);
        workspace.setSelectionBarVisible(false);
        workspace.getElement().setAttribute("data-testid", "customers-workspace");

        add(header, toolbar, workspace);
        expand(workspace);
    }

    @Override
    protected PermissionCode requiredPermission() {
        return REQUIRED_PERMISSION;
    }

    private HorizontalLayout actions(Customer customer, AuthorizationService authorization) {
        var details = new Button(VaadinIcon.EYE.create(), event -> showDetails(customer));
        details.setTooltipText(getTranslation("customers.details"));
        details.setAriaLabel(getTranslation("customers.details-aria", customer.name()));
        var edit = new Button(VaadinIcon.EDIT.create(), event -> edit(customer));
        edit.setTooltipText(getTranslation("customers.edit"));
        edit.setAriaLabel(getTranslation("customers.edit"));
        edit.setVisible(authorization.hasPermission(requireCurrentUser(), UPDATE));
        var delete = new Button(VaadinIcon.TRASH.create(), event -> confirmDelete(customer));
        delete.setTooltipText(getTranslation("customers.delete"));
        delete.setAriaLabel(getTranslation("customers.delete"));
        delete.setVisible(authorization.hasPermission(requireCurrentUser(), DELETE));
        var attachments = new Button(VaadinIcon.PAPERCLIP.create(), event -> showAttachments(customer, authorization));
        attachments.setTooltipText(getTranslation("customers.attachments"));
        attachments.setAriaLabel(getTranslation("customers.attachments"));
        var actions = new HorizontalLayout(details, edit, attachments, delete);
        actions.setPadding(false);
        actions.setSpacing(true);
        return actions;
    }

    private void showDetails(Customer customer) {
        var dialog = DetailDialog.translated("customers.details-title");
        dialog.addField(getTranslation("customers.name"), customer.name());
        dialog.addField(getTranslation("customers.email"), customer.email());
        dialog.addField(getTranslation("customers.status"), getTranslation(customer.active() ? "customers.enabled" : "customers.disabled"));
        dialog.open();
    }

    private void edit(Customer customer) {
        var name = new TextField(getTranslation("customers.name"));
        name.setRequired(true);
        var email = new TextField(getTranslation("customers.email"));
        email.setRequired(true);
        var active = new Checkbox(getTranslation("customers.enabled"));
        if (customer != null) {
            name.setValue(customer.name());
            email.setValue(customer.email());
            active.setValue(customer.active());
        } else {
            active.setValue(true);
        }
        var dialog = EditorDialog.translated(customer == null ? "customers.create-title" : "customers.edit-title", "customers.save", () -> { });
        dialog.getPrimaryAction().addClickListener(event -> {
            if (name.getValue().isBlank() || email.getValue().isBlank()) {
                dialog.showValidationMessage(getTranslation("customers.required"));
                return;
            }
            try {
                if (customer == null) {
                    customers.create(requireCurrentUser(), name.getValue(), email.getValue());
                } else {
                    customers.update(requireCurrentUser(), customer.id(), name.getValue(), email.getValue(), active.getValue());
                }
                dialog.close();
                pages.refresh();
                feedback.success(getTranslation(customer == null ? "customers.created" : "customers.updated"));
            } catch (BusinessFailure failure) {
                ViewBusinessFailureHandler.handle(failure,
                        validationFailure -> dialog.showValidationMessage(customerValidationMessage(validationFailure)));
            }
        });
        dialog.addField(name, email, active);
        dialog.open();
    }

    private void confirmDelete(Customer customer) {
        var confirmation = ConfirmationDialog.translated("customers.delete-title", "customers.delete-consequence", "customers.delete", () -> {
            customers.delete(requireCurrentUser(), customer.id());
            pages.refresh();
            feedback.success(getTranslation("customers.deleted"));
        });
        confirmation.open();
    }

    private void showAttachments(Customer customer, AuthorizationService authorization) {
        var dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("customers.attachments-title"));
        var attachments = new Grid<>(CustomerAttachment.class, false);
        attachments.addColumn(CustomerAttachment::filename).setHeader(getTranslation("customers.filename")).setAutoWidth(true);
        attachments.addColumn(CustomerAttachment::contentType).setHeader(getTranslation("customers.type"));
        attachments.addColumn(CustomerAttachment::size).setHeader(getTranslation("customers.size"));
        attachments.addComponentColumn(attachment -> new Anchor(DownloadHandler.fromInputStream(event -> {
            var download = customers.openAttachment(requireCurrentUser(), attachment.id());
            return new DownloadResponse(download.content(), download.attachment().filename(),
                    download.attachment().contentType(), download.attachment().size());
        }), getTranslation("customers.download"))).setHeader(getTranslation("customers.actions"));
        attachments.setItems(customers.attachments(requireCurrentUser(), customer.id()));
        attachments.setSizeFull();

        var upload = new Upload(com.vaadin.flow.server.streams.UploadHandler.inMemory((metadata, content) -> {
            var stored = fileUpload.store(requireCurrentUser(), ATTACHMENT_UPLOAD,
                    metadata.fileName(), metadata.contentType(), new ByteArrayInputStream(content));
            customers.attach(requireCurrentUser(), customer.id(), stored);
            attachments.setItems(customers.attachments(requireCurrentUser(), customer.id()));
            feedback.success(getTranslation("customers.uploaded"));
        }));
        upload.setAcceptedFileTypes("text/plain", ".txt", "application/pdf", ".pdf", "image/png", ".png",
                "image/jpeg", ".jpg", ".jpeg");
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.setVisible(authorization.hasPermission(requireCurrentUser(), ATTACHMENT_UPLOAD));
        dialog.add(new VerticalLayout(upload, attachments, new Button(getTranslation("flow.action.close"), event -> dialog.close())));
        dialog.open();
    }

    private String customerValidationMessage(BusinessFailure failure) {
        if (failure.fieldErrors().containsKey("name") || failure.fieldErrors().containsKey("email")) {
            return getTranslation("customers.required");
        }
        return getTranslation("customers.save-failed");
    }
}
