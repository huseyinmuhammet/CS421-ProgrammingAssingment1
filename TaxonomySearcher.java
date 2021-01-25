/*
By Hüseyin Görgülü
*/
import java.io.*;
import java.net.*;
import java.util.LinkedList;

/** This class will communicate with Server.py that download three images by traversing random folders.
 * Sends commands to communicate with Server.py and receiver responses.
 */
class TaxonomySearcher {

    /**
     * This method divide response message into pieces and return target subfolders
     * @param input string of files in one folder and status response
     * @return list of that file without response message
     */
    public static LinkedList countWords(String input) {
        LinkedList<String> list = new LinkedList<String>();
        if (input == null || input.isEmpty()) {
            return list;
        }
        String[] words = input.split("\\s+");
        for(int i = 1; i < words.length; i++){
            list.add(words[i]);
        }
        return list;
    }

    /**
     * This method returns a single file by checking whether it is downloadable and visited
     * @param visitedList is the list of visited files
     * @param node is the current list that checks whether this file is downloadable
     * @param downloadList is the target files which have to download
     * @return a single file which has processed
     */
    public static String NLST(LinkedList visitedList, LinkedList node, LinkedList downloadList ) {
        if (node.isEmpty())
            return "-1";
        for(int i = 0; i < node.size(); i++){
            if(!visitedList.contains(node.get(i))) {
                if (downloadList.contains(node.get(i)))
                    return (String) node.get(i);
                if (!node.get(i).toString().contains("."))
                    return (String) node.get(i);
                }
            }
        return "-1";
    }

    public static void main(String[] args) throws IOException{
        // When the downloadable file was found, it will be true
        boolean checkDownload;
        // Initializations of command and response strings
        String command;
        String response;
        // # of Downloaded image, if it is 3 the program will terminate
        int noDownloaded = 0;
        // Communication initializations
        String IP = args[0];
        String PORT = args[1];
        Socket clientSocket = new Socket(IP, Integer.parseInt(PORT));
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // Authentication of Username
        System.out.print("Sending:");
        command = "USER bilkentstu";
        command = command + "\r\n";
        System.out.print(command);
        outToServer.writeBytes(command);
        response = inFromServer.readLine();
        System.out.println( response);


        // Authentication of Password
        System.out.print("Sending:" );
        command = "PASS cs421f2020";
        command = command + "\r\n";
        System.out.print(command);
        outToServer.writeBytes( command );
        response = inFromServer.readLine();
        System.out.println( response );

        // Adding the Downloadable list
        System.out.print("Sending:");
        command = "OBJ";
        command = command + "\r\n";
        System.out.print(command);
        outToServer.writeBytes( command );
        response = inFromServer.readLine();
        System.out.println( response );
        // Equalization of downloadable list
        LinkedList<String> downloadList = countWords(response);
        // visitedNode is the collection of visited files
        LinkedList<String> visitedNode = new LinkedList<String>();;

        while(true){
            // # of Downloaded image, if it is 3 the program will terminate
            if(noDownloaded == 3) {
                command = "EXIT";
                command = command + "\r\n";
                System.out.print(command);
                outToServer.writeBytes(command);
                System.exit(0);
            }
            checkDownload = false;
            // NLST Command that list the files in directory
            System.out.print("Sending:");
            command = "NLST";
            command = command + "\r\n";
            System.out.print(command);
            outToServer.writeBytes(command);
            response = inFromServer.readLine();
            System.out.println( response);
            // This will fill with possible nodes
            LinkedList<String> tempList = countWords(response);
            // tempCwdrNode is the suitable node to be examined
            String tempCwdrNode = NLST(visitedNode,tempList,downloadList );
            visitedNode.add(tempCwdrNode);
            // if all tempList were checked or there is no element on templist, go upper directory
            if (tempCwdrNode == "-1"){
                command = "CDUP";
            }
            else{
                // If download list contains temporary file
                if(downloadList.contains(tempCwdrNode)) {
                    command = "GET " + tempCwdrNode;
                    noDownloaded++;
                    checkDownload = true;
                }
                else{
                    // If file has not have family and not in the download list
                    if ( tempCwdrNode.contains(".")){
                        command = "CDUP";
                    }
                    else
                        command = "CWDR " + tempCwdrNode;
                }
            }
            System.out.print("Sending:");
            System.out.println(command);
            command = command + "\r\n";
            outToServer.writeBytes(command);
            // GET Responses
            if(checkDownload){
                InputStream in = clientSocket.getInputStream();
                int a = in.read();
                a = in.read();
                a = in.read();
                a = in.read();
                int byteSize = in.read()*256*256;
                byteSize += in.read()*256;
                byteSize += in.read();
                System.out.println("ISND");
                byte[] imageArray =  new byte[byteSize];
                int asd = in.readNBytes(imageArray,0,byteSize);
                // Creating picture
                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempCwdrNode));
                outputStream.write(imageArray);
            }
            else {
                response = inFromServer.readLine();
                System.out.println(response);
            }
        }
    }
}