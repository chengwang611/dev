package com.apress.demo.entities;
/*
 * Created by hakdogan on 01/12/2017
 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
public class Document {
    private String id;
    private String title;
    private String subject;
    private String content;
    
    
    
    
	public Document() {
		super();
	}
	public Document(String id, String title, String subject, String content) {
		super();
		this.id = id;
		this.title = title;
		this.subject = subject;
		this.content = content;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
    
    
}
