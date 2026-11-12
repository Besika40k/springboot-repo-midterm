package com.example.library.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles exceptions thrown while rendering the HTML pages.
 */
@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException e) {
        ModelAndView modelAndView =
                new ModelAndView("error/404", HttpStatus.NOT_FOUND);
        modelAndView.addObject("message", e.getMessage());
        return modelAndView;
    }

    /**
     * Sends the user back to the page they came from when an uploaded
     * file is bigger than the configured limit.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleFileTooLarge(HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage",
                "The file is too large. The maximum size is 5 MB.");
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "redirect:/novels";
        }
        return "redirect:" + referer;
    }
}
