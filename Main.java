
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        String newTypeClass = "";
        String TypeClass = "";
        String newLine = "";
        try {
            File fXmlFile = new File(args[0]);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();
            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("Violation");
            System.out.println("----------------------------");
            int temp;



            HashMap<String,HashMap<String,Integer>> lineSum = new HashMap<>();
            HashMap<String,Integer> lineSuma = new HashMap<>();
            HashMap<String,Integer> lengthlineSum = new HashMap<>();
            HashMap<String,Integer> files = new HashMap<>();
            for (temp = 0; temp < nList.getLength(); temp++) {
                boolean fileReplaced = true;
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    String fileName = eElement.getAttribute("Source");
                    int lineNumber = Integer.parseInt(eElement.getAttribute("LineNumber"))-1;
                    Path FILE_PATH = Paths.get(fileName);
                    Charset cs = StandardCharsets.UTF_8;
                    newLine = "";
                    newTypeClass = "";
                    TypeClass = "";
                    //read file into stream, try-with-resources
                    List<String> fileContent = new ArrayList<String>();
                    while(fileContent.size()==0){
                        try{
                            fileContent = new ArrayList<>(Files.readAllLines(FILE_PATH, cs));
                        }catch (MalformedInputException mlinput){
                            cs = StandardCharsets.ISO_8859_1;
                        }
                    }

                    System.out.println("Reading Line: " + temp + " Rule id : " + eElement.getAttribute("RuleId"));
                    switch (eElement.getAttribute("RuleId")){
                        case "SA1101":
                            String method = nNode.getFirstChild().toString().substring(nNode.getFirstChild().toString().indexOf("The call to")+11,nNode.getFirstChild().toString().indexOf("must begin")).trim();
                            fileContent.get(lineNumber).replace(method,"this."+method);
                            fileContent.set(lineNumber, newLine);

                            System.out.println(fileName);
                            break;
                        case "SA1122":
                            newLine = fileContent.get(lineNumber).replace("\"\"","string.Empty");
                            fileContent.set(lineNumber, newLine);
                            break;
                        case "SA1005":
                            if(fileName.contains("CADAJ500Series")){
                                System.out.println("nuevo");
                            }
                            int sum = 0;
                            if(lineSuma.get(fileName) == null){
                                lineSuma.put(fileName,0);
                                sum = lineSuma.get(fileName);
                            }else {
                                sum = lineSuma.get(fileName);
                            }
                            int line = 0;
                            line = lineNumber-sum;
                            if(fileContent.get(line).indexOf("vbtonet.com")>0){
                                lineSuma.put(fileName,lineSuma.get(fileName)+1);
                                fileContent.remove(line);
                            }else{
                                newLine = fileContent.get(line).replaceAll("//","////");
                                fileContent.set(line, newLine);
                            }
                            break;
                        case "SA1306":
                            String variable = nNode.getFirstChild().toString().substring(nNode.getFirstChild().toString().indexOf("letter:")+8,nNode.getFirstChild().toString().length()-2).trim();
                            String fileContentString = "";
                            for (String s : fileContent)
                            {
                                fileContentString += s + "\r\n";
                            }
                            Pattern patt = Pattern.compile("([^\"\\w])"+variable+"[, ;]" );
                            Matcher m = patt.matcher(fileContentString);
                            StringBuffer sb = new StringBuffer(fileContentString.length());
                            while (m.find()) {
                                String text = m.group(0);
                                m.appendReplacement(sb, Matcher.quoteReplacement(Character.isLetter(text.substring(1, 2).charAt(0))? m.group(1).concat(text.substring(1, 2).toLowerCase() + text.substring(2)): ((text.substring(2, 3).toLowerCase() + text.substring(2)))));
                            }
                            m.appendTail(sb);

                            //fileContentString = fileContentString.replaceAll("[^\"\\w]"+variable,Character.isLetter(variable.substring(0, 1).charAt(0))? (" ".concat(variable.substring(0, 1).toLowerCase() + variable.substring(1))): (" ".concat(variable.substring(1, 2).toLowerCase() + variable.substring(1))));
                            fileContent = new ArrayList<String>(Arrays.asList(sb.toString().split("\r\n")));
                            break;
						case "SA1106":
                            newLine = fileContent.get(lineNumber).replace("};","}");
                            fileContent.set(lineNumber, newLine);
                            break;
                        case "SA1024":
                            newLine = fileContent.get(lineNumber).replaceAll(":",":"+"\r\n");
                            fileContent.set(lineNumber, newLine);
                            break;
                        case "SA1009":
                            int  ColumnNumber = Integer.parseInt(eElement.getAttribute("StartColumn"))-1;
                            String beginneLine = fileContent.get(lineNumber).substring(0,ColumnNumber);
                            String endLine = fileContent.get(lineNumber).substring(ColumnNumber);
                            newLine = beginneLine.concat(" ").concat(endLine);
                            fileContent.set(lineNumber, newLine);
                            break;
                        case "SA1513":
                            sum = 0;
                            if(lineSuma.get(fileName) == null){
                                lineSuma.put(fileName,0);
                                sum = lineSuma.get(fileName);
                            }else {
                                lineSuma.put(fileName,lineSuma.get(fileName)+1);
                                sum = lineSuma.get(fileName);
                            }
                            line = 0;
                            line = lineNumber + sum;
                            fileContent.add(line+1,"");
                            break;
                        case "SA1126":
                            sum = 0;
                            String lineNumberProcessingbyFile = fileName+lineNumber;
                            if(lengthlineSum.get(lineNumberProcessingbyFile) == null){
                                lengthlineSum.put(lineNumberProcessingbyFile,0);
                                sum = 0;
                            }else {
                                lengthlineSum.put(lineNumberProcessingbyFile,lengthlineSum.get(lineNumberProcessingbyFile)+5);
                                sum = lengthlineSum.get(lineNumberProcessingbyFile);
                            }
                            ColumnNumber = Integer.parseInt(eElement.getAttribute("StartColumn"))-1;
                            beginneLine = fileContent.get(lineNumber).substring(0,ColumnNumber+sum);
                            endLine = fileContent.get(lineNumber).substring(ColumnNumber+sum);
                            newLine = beginneLine.concat("this.").concat(endLine);
                            fileContent.set(lineNumber, newLine);
                            break;
                        case "SA1000":
                            String keyword = nNode.getFirstChild().toString().substring(nNode.getFirstChild().toString().indexOf("the keyword ")+11,nNode.getFirstChild().toString().indexOf("is invalid")).replace("\'","").trim();
                            newLine = fileContent.get(lineNumber).replace(keyword + "(",keyword + " (");
                            fileContent.set(lineNumber, newLine);
                            break;
                        case "SA1121":
                            newTypeClass = nNode.getFirstChild().toString().substring(nNode.getFirstChild().toString().indexOf("type alias ")+11,nNode.getFirstChild().toString().indexOf("rather than")).replace("\'","").trim();
                            TypeClass = nNode.getFirstChild().toString().substring(nNode.getFirstChild().toString().indexOf("rather than ") + 12);
                            TypeClass = TypeClass.substring(0, TypeClass.indexOf("or")).trim();
                            newLine = fileContent.get(lineNumber).replaceAll("\\b"+TypeClass+"\\b",newTypeClass);
                            fileContent.set(lineNumber, newLine);
                            break;
                        case "SA1505":
                            sum = 0;
                            int lineToRemove = lineNumber + 1;
                            if(lineSuma.get(fileName) == null){
                                lineSuma.put(fileName,0);
                                sum = 0;
                            }else{
                                sum = lineSuma.get(fileName);
                            }
                            //Array is base 0
                            while(fileContent.get(lineToRemove - sum).isEmpty()){
                                fileContent.remove(lineToRemove - sum);
                                lineSuma.put(fileName,lineSuma.get(fileName)+1);
                                //sum = lineSum.get(fileName);
                            }
                            break;
                        case "SA1119":
                            if(Integer.parseInt(eElement.getAttribute("StartLine"))!=Integer.parseInt(eElement.getAttribute("EndLine"))){
                                break;
                            }
                            sum = 0;
                            lineNumberProcessingbyFile = fileName+lineNumber;
                            if(lengthlineSum.get(lineNumberProcessingbyFile) == null){
                                lengthlineSum.put(lineNumberProcessingbyFile,0);
                                sum = lengthlineSum.get(lineNumberProcessingbyFile);
                            }else {
                                lengthlineSum.put(lineNumberProcessingbyFile,lengthlineSum.get(lineNumberProcessingbyFile)+2);
                                sum = lengthlineSum.get(lineNumberProcessingbyFile);
                            }
                            List<String> lineContent = new ArrayList<String>();
                            lineContent = new ArrayList<>(Arrays.asList(fileContent.get(lineNumber).split("")));
                            int  startColumn = Integer.parseInt(eElement.getAttribute("StartColumn"))-1;
                            int  endColumn = Integer.parseInt(eElement.getAttribute("EndColumn"))-1;
                            lineContent.remove(endColumn-(sum));
                            lineContent.remove(startColumn-(sum));
                            StringBuilder b = new StringBuilder();
                            lineContent.forEach(b::append);
                            newLine = b.toString();

                            fileContent.set(lineNumber, newLine);
                            break;
                        case "SA1200":
                            sum = 0;
                            if(lineSuma.get(fileName) == null){
                                lineSuma.put(fileName,0);
                                sum = lineSuma.get(fileName);
                            }else {
                                lineSuma.put(fileName,lineSuma.get(fileName)+1);
                                sum = lineSuma.get(fileName);
                            }
                            lineNumber = lineNumber - sum;
                            newLine = fileContent.get(lineNumber);
                            int count = 0;
                            boolean flag = false;
                            for (String line1 : fileContent){
                                if(line1.contains("namespace")){
                                    flag = true;
                                }
                                if(flag){
                                    if(line1.contains("{")){
                                        fileContent.add(count+1," "+newLine);
                                        fileContent.remove(lineNumber);
                                        break;
                                    }
                                }
                                count++;
                            }
                            break;
                        case "SA1208":
                            count = 0;
                            List<String> listofUsingsWithSystem = new ArrayList<String>();
                            List<String> listofOtherUsings = new ArrayList<String>();
                            flag = false;
                            for (String line1 : fileContent){
                                if(line1.contains("using")){
                                    lineNumber = count;
                                    break;
                                }
                                count++;
                            }
                            for (String line2 : fileContent){
                                if(line2.contains("using System")){
                                    listofUsingsWithSystem.add(line2);

                                }else if(line2.contains("using")){
                                    listofOtherUsings.add(line2);
                                }
                            }
                            while(fileContent.get(lineNumber).contains("using")){
                                fileContent.remove(lineNumber);
                            }
                            Collections.sort(listofUsingsWithSystem);
                            Collections.sort(listofOtherUsings);
                            List<String> newList = new ArrayList<String>(listofUsingsWithSystem);
                            newList.addAll(listofOtherUsings);
                            newList.add("");
                            fileContent.addAll(lineNumber,newList);
                            break;
                        case "SA1210":
                            sum = 0;
                            if(files.get(fileName) == null){
                                files.put(fileName,0);
                                sum = files.get(fileName);
                            }else {
                                files.put(fileName,files.get(fileName)+1);
                                sum = files.get(fileName);
                            }
                            if(sum == 0){
                                count = 0;
                                listofUsingsWithSystem = new ArrayList<String>();
                                listofOtherUsings = new ArrayList<String>();
                                flag = false;
                                for (String line2 : fileContent){
                                    if(line2.contains("using")){
                                        lineNumber = count;
                                        break;
                                    }
                                    count++;
                                }

                                for (String line3 : fileContent){
                                    if(line3.contains("using System")){
                                        listofUsingsWithSystem.add(line3);
                                        flag = true;
                                    }else if(line3.contains("using")){
                                        listofOtherUsings.add(line3);
                                        flag = true;
                                    }
                                    if(flag && line3.isEmpty()){
                                        break;
                                    }
                                }
                                while(fileContent.get(lineNumber).contains("using")){
                                    fileContent.remove(lineNumber);
                                }
                                Collections.sort(listofUsingsWithSystem);
                                int size = listofUsingsWithSystem.size()-1;
                                String firstusing = listofUsingsWithSystem.get(size);
                                listofUsingsWithSystem.remove(size);
                                listofUsingsWithSystem.add(0,firstusing);
                                Collections.sort(listofOtherUsings);
                                newList = new ArrayList<String>(listofUsingsWithSystem);
                                newList.addAll(listofOtherUsings);
                                fileContent.addAll(lineNumber,newList);
                            }
                            break;
                        case "SA1510":
                            sum = 0;
                            if(lineSuma.get(fileName) == null){
                                lineSuma.put(fileName,0);
                                sum = 0;
                            }else{
                                lineSuma.put(fileName,lineSuma.get(fileName)+1);
                                sum = lineSuma.get(fileName);
                            }
                            lineNumber = lineNumber - sum;
                            fileContent.remove(lineNumber-1);
                            break;
                        case "SA1025":
                            sum = 0;
                            lineNumberProcessingbyFile = fileName + lineNumber;
                            if(lengthlineSum.get(lineNumberProcessingbyFile) == null){
                                lengthlineSum.put(lineNumberProcessingbyFile,0);
                                sum = 0;
                            }else {
                                lengthlineSum.put(lineNumberProcessingbyFile,lengthlineSum.get(lineNumberProcessingbyFile)+1);
                                sum = lengthlineSum.get(lineNumberProcessingbyFile);
                            }
                            int beginColumnNumber = Integer.parseInt(eElement.getAttribute("StartColumn"));
                            int endColumnNumber = Integer.parseInt(eElement.getAttribute("EndColumn"));
                            beginneLine = fileContent.get(lineNumber).substring(0,beginColumnNumber);
                            endLine = fileContent.get(lineNumber).substring(endColumnNumber);
                            newLine = beginneLine.concat(endLine);
                            fileContent.set(lineNumber, newLine);
                            break;
                        case "SA1503":
                            Boolean isIf = nNode.getFirstChild().toString().contains("if");
                            String typeStatement = "";
                            if(isIf){
                                typeStatement = "if";
                            }else if(nNode.getFirstChild().toString().contains("else")){
                                typeStatement = "else";
                            }
                            if(typeStatement != ""){
                                sum = 0;
                                if(lineSuma.get(fileName) == null){
                                    lineSuma.put(fileName,0);
                                    sum = 0;
                                }else{
                                    lineSuma.put(fileName,lineSuma.get(fileName)+2);
                                    sum = lineSuma.get(fileName);
                                }
                                line = 0;
                                line = lineNumber + sum;
                                if(fileContent.get(line-1).contains(typeStatement) && !fileContent.get(line-1).contains(";")){
                                    int countSpaces = fileContent.get((line-1)).substring(0,fileContent.get((line-1)).indexOf(typeStatement)).length();
                                    char[] repeat = new char[countSpaces];
                                    Arrays.fill(repeat, ' ');
                                    fileContent.add((line+1)-1,new String(repeat) + "{");
                                    fileContent.add((line+1)+1,new String(repeat) + "}");
                                }
                                else if(fileContent.get(line).contains(";")){
                                    int indexOflastParenthesis = 0;
                                    indexOflastParenthesis = checkParenthesis(fileContent.get(line));
                                    if(indexOflastParenthesis > 0){
                                        String ifStatement = fileContent.get(line).substring(0,indexOflastParenthesis);;
                                        String bodyofIf = fileContent.get(line).substring(indexOflastParenthesis);
                                        int countSpaces = fileContent.get((line)).substring(0,fileContent.get((line)).indexOf(typeStatement)).length();
                                        char[] repeat = new char[countSpaces];
                                        Arrays.fill(repeat, ' ');
                                        fileContent.set(line, ifStatement);
                                        fileContent.add(line+1,new String(repeat)+"{");
                                        fileContent.add(line+2,new String(repeat)+"    "+ bodyofIf);
                                        fileContent.add(line+3,new String(repeat)+"}");
                                        lineSuma.put(fileName,lineSuma.get(fileName)+1);
                                    }else{
                                        lineSuma.put(fileName,lineSuma.get(fileName)-2);
                                    }

                                }
                            }
                            break;
                        case "SA1515":
                            newLine = fileContent.get(lineNumber).replaceAll("//","////");
                            fileContent.set(lineNumber, newLine);
                        case "SA1600":
                            String elementType = "";
                            int colNum = Integer.parseInt(eElement.getAttribute("StartColumn"));
                            sum = 0;
                            fileName = fileName.toLowerCase();
                            if(lineSum.get(fileName) == null){
                                HashMap<String,Integer> values = new HashMap<String,Integer>();
                                values.put(Integer.toString(lineNumber),0);
                                lineSum.put(fileName,values);
                                sum = 0;
                            }else{
                                HashMap<String,Integer> values = (HashMap<String,Integer>)lineSum.get(fileName);

                                if(values.get(Integer.toString(lineNumber)) == null){
                                    for(Map.Entry<String, Integer> entry: values.entrySet()) {
                                        if(sum < entry.getValue()){
                                            sum = entry.getValue();
                                        }
                                    }
                                    values.put(Integer.toString(lineNumber),sum);
//                                    values.putAll(lineSum.get(fileName));
//                                    lineSum.get(fileName).clear();
//                                    lineSum.get(fileName).put(Integer.toString(lineNumber),sum);
//                                    lineSum.get(fileName).putAll(values);
//                                    for(Map.Entry<String, Integer> entry: values.entrySet()) {
//                                        sum += entry.getValue();
//                                    }
                                }else{
                                    fileReplaced = false;
                                    break;
                                }
                            }
                            line = 0;
                            line = lineNumber + sum;
                            if(fileContent.get(line).contains(";") && (!fileContent.get(line).contains("("))){
                                if(fileContent.get(line-1).contains("/// </summary>")){
                                    fileReplaced = false;
                                    break;
                                }
//                                HashMap<String,Integer> values = lineSum.get(fileName);
//                                values.put(Integer.toString(lineNumber),+3);
                                //lineSum.put(fileName, values);
                                CharSequence inputStr = fileContent.get((line));
                                String patternStr = "\\w";
                                Pattern pattern = Pattern.compile(patternStr);
                                Matcher matcher = pattern.matcher(inputStr);
                                int countSpaces = 0;
                                if(matcher.find()){

                                    countSpaces = matcher.start();//this will give you index
                                }
                                char[] repeat = new char[countSpaces];
                                Arrays.fill(repeat, ' ');
                                String fieldName = fileContent.get(line).substring(colNum-1);
                                fieldName = fieldName.split(" ")[0];
                                String fieldType = fileContent.get(line).substring(0,fileContent.get(line).indexOf(fieldName)).split(" ")[fileContent.get(line).substring(0,fileContent.get(line).indexOf(fieldName)).split(" ").length-1];
                                Collection<String> newComment = null;
                                if(sum==0){
                                    newComment = new ArrayList<String>(Arrays.asList(new String[] {new String(repeat) + "/// <summary>", new String(repeat) + "/// Represents a " + fieldName + " of " + fieldType+ " type",new String(repeat) + "/// </summary>" }));
                                    HashMap<String,Integer> values2 = lineSum.get(fileName);
                                    sum += 3;
                                    values2.put(Integer.toString(lineNumber),sum);
                                    lineSum.put(fileName, values2);
                                }else{
                                    newComment = new ArrayList<String>(Arrays.asList(new String[] { "",new String(repeat) + "/// <summary>", new String(repeat) + "/// Represents a " + fieldName + " of " + fieldType+ " type",new String(repeat) + "/// </summary>" }));
                                    HashMap<String,Integer> values2 = lineSum.get(fileName);
                                    sum += 4;
                                    values2.put(Integer.toString(lineNumber),sum);
                                    lineSum.put(fileName, values2);
                                }
                                fileContent.addAll(line,newComment);
                                fileReplaced = true;
                                break;
                            }
                            else if(!(fileContent.get(line).contains(";")) && fileContent.get(line).contains("(")){
                                CharSequence inputStr = fileContent.get((line));
                                String patternStr = "\\w";
                                Pattern pattern = Pattern.compile(patternStr);
                                Matcher matcher = pattern.matcher(inputStr);
                                int countSpaces = 0;
                                if(matcher.find()){

                                    countSpaces = matcher.start();//this will give you index
                                }
                                char[] repeat = new char[countSpaces];
                                Arrays.fill(repeat, ' ');
                                HashMap<String,String> paramList = null;
                                Collection<String> newComment = new ArrayList<String>(){};

                                if(fileContent.get(line).substring(fileContent.get(line).indexOf("(") + 1,fileContent.get(line).lastIndexOf(")")).split(",")[0].contains("<")){
                                    HashMap<String,Integer> values = lineSum.get(fileName);
                                    sum -= 3;
                                    values.put(Integer.toString(lineNumber),sum);
                                    lineSum.put(fileName, values);
                                    break;
                                }
                                String functionName = fileContent.get(line).substring(0,fileContent.get(line).lastIndexOf("(")).split(" ")[fileContent.get(line).substring(0,fileContent.get(line).lastIndexOf("(")).split(" ").length-1];
                                functionName = functionName.split("\\%")[0];
                                String returnType = "";
                                returnType = fileContent.get(line).substring(0,fileContent.get(line).lastIndexOf("(")).split(" ")[fileContent.get(line).substring(0,fileContent.get(line).lastIndexOf("(")).split(" ").length-2];
                                newComment.addAll(new ArrayList<String>(Arrays.asList(new String[] {new String(repeat)
                                        + "/// <summary>", new String(repeat)
                                        + "/// Function definition for " + functionName ,new String(repeat)
                                        + "/// </summary>"})));
                                HashMap<String,Integer> values = lineSum.get(fileName);
                                sum += 3;
                                values.put(Integer.toString(lineNumber),sum);
                                lineSum.put(fileName, values);
                                for (String param : fileContent.get(line).substring(fileContent.get(line).indexOf("(") + 1,fileContent.get(line).lastIndexOf(")")).split(",")) {
                                    if(param.equals("")) break;
                                    String parameterName = "";
                                    if(param.contains("=")){
                                        param = param.substring(0, param.indexOf("=")).trim();
                                    }
                                    parameterName = param.trim().split(" ")[param.trim().split(" ").length-1];

                                    newComment.add(new String(repeat) + "/// <param name=\"" + parameterName + "\">"+"Parameter " + parameterName +" of type " + param.trim().split(" ")[param.trim().split(" ").length-2] + ".</param>");
                                    HashMap<String,Integer> values2 = lineSum.get(fileName);
                                    sum += 1;
                                    values2.put(Integer.toString(lineNumber),sum);
                                    lineSum.put(fileName, values2);
                                }
                                if(!returnType.equals("void")){
                                    newComment.add(new String(repeat) + "/// <returns>" + "Return an Object of " + returnType + " type.</returns>");
                                    HashMap<String,Integer> values3 = lineSum.get(fileName);
                                    sum += 1;
                                    values3.put(Integer.toString(lineNumber),sum);
                                    lineSum.put(fileName, values3);
                                }


//                                / <summary>
//                                / -- 2015.06.22 06:12 PM by Luis Acevedo
//                                / -- Task 32642 - Function to get the appropriate connection string to connect
//                                / -- to database.
//                                / </summary>
//                                / <param name="sQL">The sql connection.</param>
//                                / <param name="applicationName">The application name.</param>
//                                / <returns>RecordSet of specific SQL statement</returns>

                                fileContent.addAll(line,newComment);
                                fileReplaced = true;
                                break;
                            }else if(fileContent.get(line).contains("class")) {
                                CharSequence inputStr = fileContent.get((line));
                                String patternStr = "\\w";
                                Pattern pattern = Pattern.compile(patternStr);
                                Matcher matcher = pattern.matcher(inputStr);
                                int countSpaces = 0;
                                if (matcher.find()) {

                                    countSpaces = matcher.start();//this will give you index
                                }
                                char[] repeat = new char[countSpaces];
                                Arrays.fill(repeat, ' ');
                                HashMap<String, String> paramList = null;
                                Collection<String> newComment = new ArrayList<String>() {};

                                if (sum != 0) {
                                    newComment.add("");
                                    HashMap<String,Integer> values = lineSum.get(fileName);
                                    sum += 1;
                                    values.put(Integer.toString(lineNumber),sum);
                                    lineSum.put(fileName, values);
                                }
                                int countLine = 0;
                                for (String lineRead : fileContent.get(line).trim().split(" ")){
                                    countLine++;
                                    if(lineRead.contains("class")){
                                        break;
                                    }
                                }
                                String className = fileContent.get(line).trim().split(" ")[countLine];
                                newComment.addAll(new ArrayList<String>(Arrays.asList(new String[]{new String(repeat)
                                        + "/// <summary>", new String(repeat)
                                        + "/// This class provides a set of fields and methods for " + className, new String(repeat)
                                        + "/// </summary>"})));
                                HashMap<String,Integer> values = lineSum.get(fileName);
                                sum += 3;
                                values.put(Integer.toString(lineNumber),sum);
                                lineSum.put(fileName, values);
                                fileContent.addAll(line, newComment);
                                fileReplaced = true;
                                break;
                            }
//                          else if(nNode.getFirstChild().toString().contains("property")) {
//                                sum = 0;
//                                if (lineSum.get(fileName) == null) {
//                                    lineSum.put(fileName, 0);
//                                    sum = 0;
//                                } else {
//                                    lineSum.put(fileName, lineSum.get(fileName) + 3);
//                                    sum = lineSum.get(fileName);
//                                }
//                                line = 0;
//                                line = lineNumber + sum;
//                                CharSequence inputStr = fileContent.get((line));
//                                String patternStr = "\\w";
//                                Pattern pattern = Pattern.compile(patternStr);
//                                Matcher matcher = pattern.matcher(inputStr);
//                                int countSpaces = 0;
//                                if (matcher.find()) {
//
//                                    countSpaces = matcher.start();//this will give you index
//                                }
//                                char[] repeat = new char[countSpaces];
//                                Arrays.fill(repeat, ' ');
//                                HashMap<String, String> paramList = null;
//                                Collection<String> newComment = new ArrayList<String>() {
//                                };
//                                if(fileContent.get(line).contains("<")){
//                                    lineSum.put(fileName,lineSum.get(fileName)-3);
//                                    break;
//                                }
//                                if (sum != 0) {
//                                    newComment.add("");
//                                    lineSum.put(fileName, lineSum.get(fileName) + 1);
//                                }
//
//                                if(fileContent.get((line+4)).contains("if")){
//                                    String returnType = fileContent.get((line+6)).substring(fileContent.get((line+6)).indexOf("=")).split("\\.")[fileContent.get((line+6)).substring(fileContent.get((line+6)).indexOf("=")).split("\\.").length-1];
//                                    returnType = returnType.contains("(") ? returnType.substring(0,returnType.indexOf("(")) : returnType.substring(0,returnType.indexOf(";"));
//                                    newComment.addAll(new ArrayList<String>(Arrays.asList(new String[]{new String(repeat)
//                                            + "/// <summary>", new String(repeat)
//                                            + "/// This property Initialize a new instance of " + returnType , new String(repeat)
//                                            + "/// </summary>"})));
//
//                                    fileContent.addAll(line, newComment);
//                                    break;
//                                }else{
//                                    String fieldName = fileContent.get((line+4)).split("\\.")[fileContent.get((line+4)).split("\\.").length-1].replace(";","");
//                                    newComment.addAll(new ArrayList<String>(Arrays.asList(new String[]{new String(repeat)
//                                            + "/// <summary>", new String(repeat)
//                                            + "/// This property will set/get " + fieldName + " field", new String(repeat)
//                                            + "/// </summary>"})));
//
//                                    fileContent.addAll(line, newComment);
//                                    break;
//                                }
//
//
//                            }
                             //newLine = fileContent.get(lineNumber).replaceAll("//","////");
                            //fileContent.set(lineNumber, newLine);
//                        case "SA1623" :
//                            CharSequence inputStr = fileContent.get((lineNumber));
//                            String patternStr = "\\w";
//                            Pattern pattern = Pattern.compile(patternStr);
//                            Matcher matcher = pattern.matcher(inputStr);
//                            int countSpaces = 0;
//                            if (matcher.find()) {
//
//                                countSpaces = matcher.start();//this will give you index
//                            }
//                            char[] repeat = new char[countSpaces];
//                            Arrays.fill(repeat, ' ');
//                            String newComment = "";
//                            if(fileContent.get(lineNumber-2).contains("instance")){
//                                newComment = new String(repeat) + "/// Gets "+ fileContent.get(lineNumber-2).substring(fileContent.get(lineNumber-2).indexOf("new instance"));
//                            }else{
//                                newComment = new String(repeat) + "/// Gets or sets "+ fileContent.get(lineNumber-2).substring(fileContent.get(lineNumber-2).indexOf("set/get")+8);
//                            }
//                            fileContent.set(lineNumber-2,newComment);
//                            break;
//                        case "SA1507":
//                            System.out.println("SA1507");
//                            List<String> tmpContent = fileContent;
//                            List<Integer> lnsToRemove = new ArrayList<Integer>();
//                            List<String> filesDone = new ArrayList<String>();
//                            if (filesDone.indexOf(fileName) == -1)
//                            {
//                                for(int i=tmpContent.size()-1; i>-1; i--)
//                                {
//                                    if (i>-1)
//                                    {
//                                        if (tmpContent.get(i).trim().length() == 0
//                                                && ((i-1)>-1)
//                                                && tmpContent.get(i - 1).trim().length() == 0)
//                                        {
//                                            lnsToRemove.add(i);
//                                        }
//                                    }
//                                }
//                                for (Integer line : lnsToRemove) {
//                                    tmpContent.remove(line.intValue());
//                                }
//                                fileContent = tmpContent;
//                                filesDone.add(fileName);
//                            }
//                            break;
//                        case "SA1514":
//                            int sum = 0;
//                            if (lineSum.get(fileName) == null) {
//                                lineSum.put(fileName, 0);
//                                sum = 0;
//                            } else {
//                                lineSum.put(fileName, lineSum.get(fileName) + 1);
//                                sum = lineSum.get(fileName);
//                            }
//                            int line = 0;
//                            line = lineNumber + sum;
//                            fileContent.add(line,"");
//                            break;
//                        case "SA1401":
//                            CharSequence inputStr = fileContent.get((lineNumber));
//                            String line = fileContent.get(lineNumber).trim();
//                            if(fileContent.get((lineNumber)).contains("public ")){
//                                line = line.replace("public ","");
//                            }
//                            String patternStr = "\\w";
//                            Pattern pattern = Pattern.compile(patternStr);
//                            Matcher matcher = pattern.matcher(inputStr);
//                            int countSpaces = 0;
//                            if (matcher.find()) {
//
//                                countSpaces = matcher.start();//this will give you index
//                            }
//                            char[] repeat = new char[countSpaces];
//                            Arrays.fill(repeat, ' ');
//                            fileContent.set(lineNumber, new String(repeat) + "private " + line);
//                            break;
//                        case "SA1649":
//                            fileName = fileName.split("\\\\")[fileName.split("\\\\").length-1].replace(".cs","");
//                            for(String line : fileContent){
//                                if(line.contains("class") && !(line.contains("///"))){
//                                    String className = line.split(" ")[line.split(" ").length-1];
//                                    line = line.replace(className,fileName);
//                                    fileContent.set(lineNumber, line);
//                                    break;
//                                }
//                                lineNumber ++;
//                            }
//                            break;
//                        case "SA1649":
                            //fileName = fileName.split("\\\\")[fileName.split("\\\\").length-1].replace(".cs","");
//                            String newFileName = nNode.getFirstChild().toString().substring(nNode.getFirstChild().toString().indexOf("these: ")+8);
//                            newFileName = newFileName.substring(0,newFileName.length()-2);
//                            newLine = fileContent.get(lineNumber);
//
//
//
//                            // File (or directory) with old name
//                            File file = new File(fileName);
//
//                            String directory = file.getParent();
//                            List<String> textFiles2 = new ArrayList<String>();
//                            File dir = new File(directory);
//                            for (File file3 : dir.listFiles()) {
//                                if (file3.getName().endsWith((".csproj"))) {
//                                    textFiles2.add(file3.getAbsolutePath());
//                                }
//                            }
//                            if(textFiles2.size()>1){
//                                fileReplaced = false;
//                                break;
//                            }
//
//                            newLine = newLine.replace(file.getName(),newFileName + ".cs");
//                            fileContent.set(lineNumber,newLine);
//
//                            Files.write(FILE_PATH, fileContent, StandardCharsets.UTF_8);
//
//                            // File (or directory) with new name
//                            File file2 = new File(file.getParent() + "\\" + newFileName + ".cs");
//
//
//                            if (file2.exists()){
//                                throw new java.io.IOException("file exists");
//                            }
//                            // Rename file (or directory)
//                            boolean success = file.renameTo(file2);
//
//                            if (success) {
//                                if(textFiles2.size() == 1){
//                                    Path path = Paths.get(textFiles2.get(0));
//                                    Charset charset = StandardCharsets.UTF_8;
//
//                                    String content = new String(Files.readAllBytes(path), charset);
//                                    content = content.replace(file.getName(), newFileName + ".cs");
//                                    Files.write(path, content.getBytes(charset));
//
//                                }
//                                fileReplaced = false;
//                            }
//                            break;
                        default:
                            fileReplaced = false;
                            System.out.println("StyelCop Rule not handled");
                            break;
                    }
                    if(fileReplaced){
                        //replacing new line in file
                        Files.write(FILE_PATH, fileContent, StandardCharsets.UTF_8);
                    }

                }
            }
            System.out.println("Total lines readed: " + temp);
        }catch (Exception ex){
            System.out.println("Exception processing file: " + ex);
        }
    }

    public static <K, V> void add(HashMap<K, V> map, int index, K key, V value) {
        assert (map != null);
        assert !map.containsKey(key);
        assert (index >= 0) && (index < map.size());

        int i = 0;
        List<Map.Entry<K, V>> rest = new ArrayList<Map.Entry<K, V>>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (i++ >= index) {
                rest.add(entry);
            }
        }
        map.put(key, value);
        for (int j = 0; j < rest.size(); j++) {
            Map.Entry<K, V> entry = rest.get(j);
            map.remove(entry.getKey());
            map.put(entry.getKey(), entry.getValue());
        }
    }

    public static int checkParenthesis(String linewithIf)
    {
        int endParentesisOfIf = 0;
        Stack<Integer> stk = new Stack<Integer>();
        String exp = linewithIf;
        int len = exp.length();
        System.out.println("\nMatches and Mismatches:\n");
        for (int i = 0; i < len; i++)
        {
            char ch = exp.charAt(i);
            if (ch == '(')
                stk.push(i);
            else if (ch == ')')
            {
                try
                {
                    int p = stk.pop() + 1;
                    System.out.println("'(' at index "+(i+1)+" matched with ')' at index "+p);
                    endParentesisOfIf = (i+1);
                    break;
                }
                catch(Exception e)
                {
                    System.out.println("')' at index "+(i+1)+" is unmatched");
                }
            }
        }
        while (!stk.isEmpty() ){
            System.out.println("'(' at index "+(stk.pop() +1)+" is unmatched");
        }
        return endParentesisOfIf;
    }

}
