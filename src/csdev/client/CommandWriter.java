package remoteshell;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class CommandWriter {
    private DataOutputStream out;
    private boolean connected;
    private Socket socket;
    
    public CommandWriter(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new DataOutputStream(socket.getOutputStream());
        this.connected = true;
        sendConnect();
    }
    
    private void sendConnect() throws IOException {
        if (connected) {
            out.writeInt(ProtocolConstants.CMD_CONNECT);
            out.flush();
        }
    }
    
    public void sendCommand(String command) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to server");
        }
        
        if (command.length() > ProtocolConstants.MAX_COMMAND_LENGTH) {
            throw new IOException("Command too long");
        }
        
        out.writeInt(ProtocolConstants.CMD_EXECUTE);
        byte[] commandBytes = command.getBytes(ProtocolConstants.ENCODING);
        out.writeInt(commandBytes.length);
        out.write(commandBytes);
        out.flush();
    }
    
    public void sendChangeDirectory(String path) throws IOException {
        sendCommand(ProtocolConstants.SPECIAL_CD + path);
    }
    
    public void sendPwdCommand() throws IOException {
        sendCommand(ProtocolConstants.SPECIAL_PWD);
    }
    
    public void sendLsCommand(String args) throws IOException {
        String command = ProtocolConstants.SPECIAL_LS;
        if (args != null && !args.isEmpty()) {
            command += " " + args;
        }
        sendCommand(command);
    }
    
    public void sendListUsers() throws IOException {
        if (connected) {
            try {
                out.writeInt(ProtocolConstants.CMD_LIST_USERS);
                out.flush();
            } catch (IOException e) {
                sendCommand("users");
            }
        }
    }
    
    public void sendExitCommand() throws IOException {
        if (connected) {
            out.writeInt(ProtocolConstants.CMD_DISCONNECT);
            out.flush();
            connected = false;
        }
    }
    
    public void close() {
        connected = false;
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                System.err.println("Error closing output stream");
            }
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
}