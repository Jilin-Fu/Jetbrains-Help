package com.jetbrains.help.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.jetbrains.help.context.AgentContextHolder;
import com.jetbrains.help.context.PluginsContextHolder;
import com.jetbrains.help.context.ProductsContextHolder;
import com.jetbrains.help.properties.JetbrainsHelpProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

@Controller
@RequiredArgsConstructor
public class IndexController {
    private final JetbrainsHelpProperties jetbrainsHelpProperties;

    @Data
    public static class UpdateLicenseInfoReqBody {

        private String licenseeName;

        private String assigneeName;

        private String expiryDate;
    }

    @GetMapping
    public String index(Model model) {
        List<ProductsContextHolder.ProductCache> productCacheList = ProductsContextHolder.productCacheList();
        List<PluginsContextHolder.PluginCache> pluginCacheList = PluginsContextHolder.pluginCacheList();
        model.addAttribute("products", productCacheList);
        model.addAttribute("plugins", pluginCacheList);
        model.addAttribute("defaults", jetbrainsHelpProperties);
        return "index";
    }

    @GetMapping("search")
    public String index(@RequestParam(required = false) String search, Model model) {
        List<ProductsContextHolder.ProductCache> productCacheList = ProductsContextHolder.productCacheList();
        List<PluginsContextHolder.PluginCache> pluginCacheList = PluginsContextHolder.pluginCacheList();
        if (CharSequenceUtil.isNotBlank(search)) {
            productCacheList = productCacheList.stream()
                    .filter(productCache -> CharSequenceUtil.containsIgnoreCase(productCache.getName(), search))
                    .toList();
            pluginCacheList = pluginCacheList.stream()
                    .filter(pluginCache -> CharSequenceUtil.containsIgnoreCase(pluginCache.getName(), search))
                    .toList();
        }
        model.addAttribute("products", productCacheList);
        model.addAttribute("plugins", pluginCacheList);
        model.addAttribute("defaults", jetbrainsHelpProperties);
        return "index::product-list";
    }

    @GetMapping("ja-netfilter")
    @ResponseBody
    public ResponseEntity<Resource> downloadJaNetfilter() {
        File jaNetfilterZipFile = AgentContextHolder.jaNetfilterZipFile();
        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, "attachment;filename=" + jaNetfilterZipFile.getName())
                .contentType(APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(FileUtil.getInputStream(jaNetfilterZipFile)));
    }

    @PostMapping("updateLicenseInfo")
    @ResponseBody
    public String generateLicense(@RequestBody UpdateLicenseInfoReqBody body) {
        String licenseeName = body.getLicenseeName();
        String assigneeName = body.getAssigneeName();
        String expiryDate = body.getExpiryDate();

        if (licenseeName == null || licenseeName.isEmpty() ||
                assigneeName == null || assigneeName.isEmpty() ||
                expiryDate == null || !Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$").matcher(expiryDate).matches()) {
            return STR."参数异常[licenseeName=\{licenseeName},assigneeName=\{assigneeName},expiryDate=\{expiryDate},]";
        }

        jetbrainsHelpProperties.setDefaultLicenseName(body.getLicenseeName());
        jetbrainsHelpProperties.setDefaultAssigneeName(body.getAssigneeName());
        jetbrainsHelpProperties.setDefaultExpiryDate(body.getExpiryDate());
        return "";
    }
}
