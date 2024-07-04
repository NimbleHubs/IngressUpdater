package com.open.ingressupdater;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/ingress")
public class IngressController {

    private final KubernetesClient kubernetesClient;

    public IngressController() {
        Config config = Config.autoConfigure(null);
        this.kubernetesClient = new DefaultKubernetesClient(config);
    }

    @PostMapping("/update-ip-whitelist")
    public String updateIpWhitelist(@RequestParam String namespace,
                                    @RequestParam String ingressName,
                                    @RequestParam String ipCidr) {

        try {
            // 获取现有的 Ingress
            Ingress ingress = kubernetesClient.network().v1().ingresses().inNamespace(namespace).withName(ingressName).get();

            if (ingress != null) {
                // 更新 Ingress 的注解以添加 IP 白名单
                if (ingress.getMetadata().getAnnotations() == null) {
                    ingress.getMetadata().setAnnotations(new HashMap<>());
                }
                ingress.getMetadata().getAnnotations().put("nginx.ingress.kubernetes.io/whitelist-source-range", ipCidr);

                // 应用更新后的 Ingress
                kubernetesClient.network().v1().ingresses().inNamespace(namespace).withName(ingressName).replace(ingress);

                return "Updated Ingress with new IP whitelist: " + ipCidr;
            } else {
                return "Ingress not found: " + ingressName;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error updating Ingress: " + e.getMessage();
        }
    }
}
