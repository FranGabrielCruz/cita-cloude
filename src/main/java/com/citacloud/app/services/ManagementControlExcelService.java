package com.citacloud.app.services;
import com.citacloud.app.security.*; import org.springframework.stereotype.Service; import java.util.*;
/** Bean sin clases POI en sus firmas: compatible con reinicios DevTools. */
@Service public class ManagementControlExcelService {
 private final ManagementControlReportService reportes; private final AuditoriaService auditoria;
 public ManagementControlExcelService(ManagementControlReportService r,AuditoriaService a){reportes=r;auditoria=a;}
 public byte[] generar(UUID empresa,ManagementControlReportService.Filtros filtros,String clinica){TenantUserDetails u=AuthService.getAuthenticatedUser();if(u==null||!empresa.equals(u.getEmpresaId())||!puede(u))throw new IllegalArgumentException("No tienes permiso para exportar este reporte.");var reporte=reportes.generar(empresa,filtros);byte[] archivo=ManagementControlExcelWriter.generar(reporte,reportes.citasDelReporte(empresa,filtros),clinica,filtros);auditoria.registrar(empresa,u.getUsuarioId(),"REPORTES","REPORT_EXPORTED","MANAGEMENT_CONTROL",null,"Gestión y Control Excel",null,List.of(),"SUCCESS",filtros.desde()+" a "+filtros.hasta(),false);return archivo;}
 private boolean puede(TenantUserDetails u){return u.getAuthorities().stream().anyMatch(a->"reports.management.export.excel".equals(a.getAuthority())||"ROLE_ADMINISTRADOR".equals(a.getAuthority())||"ROLE_SUPERADMIN".equals(a.getAuthority()));}
}
