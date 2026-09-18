package com.citacloud.app.services;
import org.springframework.stereotype.Service; import java.util.*; import java.util.regex.*;
@Service public class PlantillaNotificacionService {
 public static final Set<String> VARIABLES=Set.of("paciente","fecha_cita","hora_cita","medico","especialidad","sucursal","consultorio","clinica"); private static final Pattern P=Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*}}");
 public void validar(String plantilla){if(plantilla==null||plantilla.isBlank())throw new IllegalArgumentException("La plantilla es obligatoria.");Matcher m=P.matcher(plantilla);while(m.find())if(!VARIABLES.contains(m.group(1)))throw new IllegalArgumentException("La variable {{"+m.group(1)+"}} no es válida."); String sin=plantilla.replaceAll("\\{\\{\\s*[a-zA-Z0-9_]+\\s*}}",""); if(sin.contains("{{")||sin.contains("}}"))throw new IllegalArgumentException("La plantilla contiene una variable incompleta.");}
 public String renderizar(String plantilla,Map<String,String> valores){validar(plantilla);String resultado=plantilla;for(String v:VARIABLES)resultado=resultado.replaceAll("\\{\\{\\s*"+Pattern.quote(v)+"\\s*}}",Matcher.quoteReplacement(valores.getOrDefault(v,"—")));return resultado;}
 public Map<String,String> ejemplo(){return Map.of("paciente","Ana Pérez","fecha_cita","10/09/2026","hora_cita","10:30 AM","medico","Dr. Juan Pérez","especialidad","Cardiología","sucursal","Clínica Principal","consultorio","Consultorio 2","clinica","Clínica Central");}
}
