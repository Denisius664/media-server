package inc.sneid.media_server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.logging.Logger;

@org.springframework.stereotype.Controller
public class ImageController {

    private final Logger log = Logger.getLogger("image logger".toUpperCase());

    @Value("${path.image}")
    private String pathImage;

    @GetMapping("image/{imageName}")
    public void getFile(@PathVariable String imageName,
                        HttpServletResponse response) {
        try{
            // get your file as InputStream
            File file = new File("src/main/resources/image/" + imageName);
            if (file.exists()) {
                InputStream is = new FileInputStream(file);
                // copy it to response's OutputStream
                org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
                response.flushBuffer();
            }else{
                log.info(String.format("Error writing file to output stream. Filename was '{%s}'", imageName));
            }
        } catch (IOException e) {
            throw new RuntimeException("IOError writing file to output stream");
        }
    }

    @PostMapping("image")
    public String getImage(@RequestParam MultipartFile image,
                           @RequestParam(required = false, defaultValue = "false") boolean needAvatar) throws IOException {
        if (image != null && !image.getOriginalFilename().isEmpty()
                && image.getContentType().contains("image")) {

            //get different names
            String[] splitFileName = image.getOriginalFilename().split("\\.");
            String fileExtension = "." + splitFileName[splitFileName.length - 1];
            String fileName = splitFileName[0];

            System.out.println("Image name is " + fileName);
            image.transferTo(Path.of("src/main/resources/image/" + fileName + fileExtension));
            if (needAvatar) {
                String imageAvatar = fileName + "_avatar";
                cropAndCompress(image, "src/main/resources/image/" + imageAvatar + fileExtension);
            }
            return "success";
        }else{
            System.out.println("Error");
            return "error";
        }
    }

    public void cropAndCompress(MultipartFile image, String to){
        //crop
        InputStream imageInputStream = null;
        try {
            imageInputStream = image.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedImage in = null;
        try {
            in = ImageIO.read(imageInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int width = in.getWidth();
        int height = in.getHeight();
        int segment;
        BufferedImage out;
        if (width > height) {
            segment = (width - height) / 2;
            out = in.getSubimage(segment, 0, height, height);
        } else {
            segment = (height - width) / 2;
            out = in.getSubimage(0, segment, width, width);
        }
        //Compress
        OutputStream os = null;
        try {
            os = new FileOutputStream(to);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ImageOutputStream ios = null;
        try {
            ios = ImageIO.createImageOutputStream(os);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterator<ImageReader> readers = null;
        try {
            readers = ImageIO.getImageReaders(ImageIO.createImageInputStream(imageInputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String formatName = null;
        if (readers.hasNext()) {
            try {
                formatName = readers.next().getFormatName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(formatName);
        } else {
            formatName = "JPEG";
        }
        ImageWriter writer = ImageIO.getImageWritersByFormatName(formatName).next();
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.3f);

        try {
            writer.write(null, new IIOImage(out, null, null), param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
