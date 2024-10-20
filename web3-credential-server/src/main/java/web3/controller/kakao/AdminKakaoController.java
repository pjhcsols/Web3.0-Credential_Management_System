package web3.controller.kakao;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import web3.properties.KakaoProperties;

@Controller
@RequestMapping("/admin/kakao")
public class AdminKakaoController {

    private final KakaoProperties kakaoProperties;

    public AdminKakaoController(KakaoProperties kakaoProperties) {
        this.kakaoProperties = kakaoProperties;
    }

    @GetMapping
    public String kakaoLoginPage(Model model) {
        model.addAttribute("kakao", kakaoProperties);
        return "kakao";
    }
}
