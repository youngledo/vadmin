package io.github.vaadinadminstarter.app.customer;

import java.io.InputStream;

public record CustomerAttachmentDownload(CustomerAttachment attachment, InputStream content) { }
