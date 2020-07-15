package inc.sneid.media_server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Value("${path.image}")
    private String pathImage;

    @GetMapping
    public String home(Model model){
        File[] images = new File(pathImage).listFiles();
        if (images != null) {
            model.addAttribute("files",
                    Arrays.stream(images).map(File::getName).collect(Collectors.toList()));
        }
        return "home";
    }

}
