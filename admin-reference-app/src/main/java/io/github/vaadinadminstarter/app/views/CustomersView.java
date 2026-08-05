package io.github.vaadinadminstarter.app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import io.github.vaadinadminstarter.app.customer.Customer;
import io.github.vaadinadminstarter.app.customer.CustomerAttachment;
import io.github.vaadinadminstarter.app.customer.CustomerService;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.contracts.error.BusinessFailure;
import io.github.vaadinadminstarter.contracts.file.FileStorage;
import io.github.vaadinadminstarter.flow.patterns.DataWorkspace;
import io.github.vaadinadminstarter.flow.patterns.EditorDialog;
import io.github.vaadinadminstarter.flow.patterns.FlowFileUpload;
import io.github.vaadinadminstarter.flow.patterns.PagedGrid;
import io.github.vaadinadminstarter.flow.patterns.PageHeader;
import io.github.vaadinadminstarter.flow.patterns.PageToolbar;
import io.github.vaadinadminstarter.springsecurity.auth.SecurityContextCurrentUserProvider;
import java.io.ByteArrayInputStream;
import java.util.Map;

@Route(value = "customers", layout = MainLayout.class)
@PageTitle("客户")
public final class CustomersView extends SecuredView {
    private static final PermissionCode CREATE = PermissionCode.of("customer:customer:create");
    private static final PermissionCode UPDATE = PermissionCode.of("customer:customer:update");
    private static final PermissionCode DELETE = PermissionCode.of("customer:customer:delete");
    private static final PermissionCode ATTACHMENT_UPLOAD = PermissionCode.of("customer:attachment:upload");

    private final CustomerService customers;
    private final FlowFileUpload fileUpload;
    private final Grid<Customer> grid = new Grid<>(Customer.class, false);
    private final TextField filter = new TextField("搜索");
    private final PagedGrid<Customer> pages;

    public CustomersView(SecurityContextCurrentUserProvider currentUser, AuthorizationService authorization,
                         CustomerService customers, FileStorage fileStorage) {
        super(currentUser, authorization);
        this.customers = customers;
        this.fileUpload = new FlowFileUpload(authorization, fileStorage);
        filter.setPlaceholder("名称或邮箱");
        filter.setClearButtonVisible(true);
        filter.setValueChangeMode(ValueChangeMode.EAGER);

        grid.addColumn(Customer::name).setHeader("名称").setAutoWidth(true);
        grid.addColumn(Customer::email).setHeader("邮箱").setAutoWidth(true);
        grid.addColumn(customer -> customer.active() ? "启用" : "停用").setHeader("状态");
        grid.addComponentColumn(customer -> actions(customer, authorization)).setHeader("操作").setAutoWidth(true);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        pages = new PagedGrid<>(grid, query -> customers.page(requireCurrentUser(), query),
                () -> Map.of("q", filter.getValue()), "name");
        filter.addValueChangeListener(event -> pages.refresh());

        var header = new PageHeader("客户", "维护客户档案，并在需要时管理其受控附件。");
        var toolbar = new PageToolbar();
        toolbar.getElement().setAttribute("data-testid", "customers-toolbar");
        toolbar.addFilter(filter);
        var create = new Button("新增客户", VaadinIcon.PLUS.create(), event -> edit(null));
        create.setVisible(authorization.hasPermission(requireCurrentUser(), CREATE));
        toolbar.setPrimaryAction(create);
        var workspace = new DataWorkspace<>(grid);
        workspace.setSelectionBarVisible(false);
        workspace.getElement().setAttribute("data-testid", "customers-workspace");

        add(header, toolbar, workspace);
        expand(workspace);
    }

    @Override
    PermissionCode requiredPermission() {
        return PermissionCode.of("customer:customer:read");
    }

    private HorizontalLayout actions(Customer customer, AuthorizationService authorization) {
        var edit = new Button(VaadinIcon.EDIT.create(), event -> edit(customer));
        edit.setTooltipText("编辑客户");
        edit.setAriaLabel("编辑客户");
        edit.setVisible(authorization.hasPermission(requireCurrentUser(), UPDATE));
        var delete = new Button(VaadinIcon.TRASH.create(), event -> confirmDelete(customer));
        delete.setTooltipText("删除客户");
        delete.setAriaLabel("删除客户");
        delete.setVisible(authorization.hasPermission(requireCurrentUser(), DELETE));
        var attachments = new Button(VaadinIcon.PAPERCLIP.create(), event -> showAttachments(customer, authorization));
        attachments.setTooltipText("客户附件");
        attachments.setAriaLabel("客户附件");
        return new HorizontalLayout(edit, attachments, delete);
    }

    private void edit(Customer customer) {
        var name = new TextField("名称");
        name.setRequired(true);
        var email = new TextField("邮箱");
        email.setRequired(true);
        var active = new Checkbox("启用");
        if (customer != null) {
            name.setValue(customer.name());
            email.setValue(customer.email());
            active.setValue(customer.active());
        } else {
            active.setValue(true);
        }
        var dialog = new EditorDialog(customer == null ? "新增客户" : "编辑客户", "保存", () -> { });
        dialog.getCancelAction().setText("取消");
        dialog.getPrimaryAction().addClickListener(event -> {
            if (name.getValue().isBlank() || email.getValue().isBlank()) {
                dialog.showValidationMessage("名称和邮箱均为必填项。");
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
            } catch (BusinessFailure failure) {
                dialog.showValidationMessage(customerValidationMessage(failure));
            }
        });
        dialog.addField(name, email, active);
        dialog.open();
    }

    private void confirmDelete(Customer customer) {
        var confirmation = new ConfirmDialog("删除客户", "删除后无法恢复。", "删除", event -> {
            customers.delete(requireCurrentUser(), customer.id());
            pages.refresh();
        });
        confirmation.setCancelable(true);
        confirmation.open();
    }

    private void showAttachments(Customer customer, AuthorizationService authorization) {
        var dialog = new Dialog();
        dialog.setHeaderTitle("客户附件");
        var attachments = new Grid<>(CustomerAttachment.class, false);
        attachments.addColumn(CustomerAttachment::filename).setHeader("文件名").setAutoWidth(true);
        attachments.addColumn(CustomerAttachment::contentType).setHeader("类型");
        attachments.addColumn(CustomerAttachment::size).setHeader("大小");
        attachments.addComponentColumn(attachment -> new Anchor(DownloadHandler.fromInputStream(event -> {
            var download = customers.openAttachment(requireCurrentUser(), attachment.id());
            return new DownloadResponse(download.content(), download.attachment().filename(),
                    download.attachment().contentType(), download.attachment().size());
        }), "下载")).setHeader("操作");
        attachments.setItems(customers.attachments(requireCurrentUser(), customer.id()));
        attachments.setSizeFull();

        var upload = new Upload(com.vaadin.flow.server.streams.UploadHandler.inMemory((metadata, content) -> {
            var stored = fileUpload.store(requireCurrentUser(), ATTACHMENT_UPLOAD,
                    metadata.fileName(), metadata.contentType(), new ByteArrayInputStream(content));
            customers.attach(requireCurrentUser(), customer.id(), stored);
            attachments.setItems(customers.attachments(requireCurrentUser(), customer.id()));
        }));
        upload.setAcceptedFileTypes("text/plain", ".txt", "application/pdf", ".pdf", "image/png", ".png",
                "image/jpeg", ".jpg", ".jpeg");
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.setVisible(authorization.hasPermission(requireCurrentUser(), ATTACHMENT_UPLOAD));
        dialog.add(new VerticalLayout(upload, attachments, new Button("关闭", event -> dialog.close())));
        dialog.open();
    }

    private String customerValidationMessage(BusinessFailure failure) {
        if (failure.fieldErrors().containsKey("name") || failure.fieldErrors().containsKey("email")) {
            return "名称和邮箱均为必填项。";
        }
        return "无法保存客户，请检查输入后重试。";
    }
}
