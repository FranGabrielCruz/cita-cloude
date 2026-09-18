package com.citacloud.app.services;
public interface WhatsappProvider {boolean configurado(); Resultado enviar(String destinatario,String mensaje,String templateId); record Resultado(String proveedor,String referencia){} }
