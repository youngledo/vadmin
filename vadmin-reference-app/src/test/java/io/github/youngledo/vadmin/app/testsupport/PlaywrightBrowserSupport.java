package io.github.youngledo.vadmin.app.testsupport;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import java.util.List;
import java.util.Map;

/** Test-only browser setup that isolates UI checks from CI-host browser add-ons. */
public final class PlaywrightBrowserSupport {
    private static final String REMOVE_EXTERNAL_OVERLAY = """
            () => {
              const removeInjectedOverlay = () => {
                document.querySelectorAll('copilot-main').forEach((element) => element.remove());
              };
              new MutationObserver(removeInjectedOverlay).observe(document, { childList: true, subtree: true });
              removeInjectedOverlay();
            }
            """;

    private PlaywrightBrowserSupport() { }

    public static Playwright createPlaywright() {
        return Playwright.create(new Playwright.CreateOptions()
                .setEnv(Map.of("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")));
    }

    public static Browser launchChromium(Playwright playwright) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setArgs(List.of("--disable-extensions", "--disable-component-extensions-with-background-pages")));
    }

    public static BrowserContext newContext(Browser browser, Browser.NewContextOptions options) {
        var context = browser.newContext(options);
        context.addInitScript(REMOVE_EXTERNAL_OVERLAY);
        return context;
    }
}
