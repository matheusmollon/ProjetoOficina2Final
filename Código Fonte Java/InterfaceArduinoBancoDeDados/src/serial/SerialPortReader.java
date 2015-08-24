package serial;

import entity.Measure;
import db.DBConnector;
import db.MeasureDAO;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;
import javax.swing.JTextArea;

public class SerialPortReader extends Thread implements SerialPortEventListener {

    // Objeto que guarda o nome da porta serial escolhida na Interface
    private String selectedPort = null;

    // Objeto de DBConnector
    private DBConnector dbConn = null;

    // Objeto de MeasureDAO
    private MeasureDAO meDAO = null;

    // Objeto de SerialPort
    private SerialPort serialPort = null;

    // Objeto de BufferedReader
    private InputStream input = null;

    // Tempo máximo para tentar abrir a porta serial
    private final int TIME_OUT = 2000;

    // Taxa de transmissão de dados da porta serial 9600bps 
    private final int DATA_RATE = 9600;

    // Flag para indicar que a porta serial está conectada
    private boolean connected = false;

    // Receive the byte comming from the serial port
    private String dataReceived = "";

    // Concatena os dados recebidos da serial
    private String answer = "";

    // Armazena o valor da temperatura
    private String temperature = "";

    // Armazena o valor da umidade
    private String humidity = "";

    // Flag para indicar que os dados estão sendo recebidos
    private boolean pakageReceived = false;

    // JTextArea da Interface, para printLog as mensagens
    private JTextArea log = null;

    // Construtor de SerialPortReader
    public SerialPortReader() {

    }

    // Construtor de SerialPortReader sobrecarregado
    public SerialPortReader(String selectedPort, JTextArea log) {

        this.selectedPort = selectedPort;
        this.log = log;
    }

    // Método chamado pela interface para pegar os identificadores das ports
    // seriais
    public Enumeration getCommPortIdentifiers() {

        return CommPortIdentifier.getPortIdentifiers();
    }

    // Método para printLog as mensagens no JTextArea
    public void printLog(String s) {

        log.append(s + "\n");
    }

    // Quando o SerialPortReader é iniciado vem para o run()
    @Override
    public void run() {

        // Início da classe responsável pela leitura da porta serial
        printLog("Começou: " + this.getName());

        // Instância o objeto de DBConnector
        dbConn = new DBConnector();

        // Instância o objeto de armazenar no banco de dados
        meDAO = new MeasureDAO();

        // Tenta abrir a conexão com o banco de dados
        try {
            dbConn.openConnection();
        } catch (ClassNotFoundException | SQLException ex) {
            printLog("Erro de Banco de Dados: " + ex.toString());
        }

        // Obtém um HashMap com todas as CommPortIdentifiers associadas ao Nome
        HashMap commPorts = getCommPortIdentifiersByName();

        // Escolhe a CommPortIdentifier pelo nome selecionado na Interface
        CommPortIdentifier portName = (CommPortIdentifier) commPorts.get(selectedPort);

        // Tenta abrir a porta serial
        try {
            // Abre a porta serial e usa o nome da classe como app name
            serialPort = (SerialPort) portName.open(this.getClass().getName(),
                    TIME_OUT);

            // Passa os parâmetros de comunicação da porta serial
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            connected = true;
        } catch (PortInUseException | UnsupportedCommOperationException ex) {
            printLog("Erro da Porta Serial: " + ex.toString());
        } catch (NullPointerException ex) {
            printLog("Erro da Porta Serial: Porta Desconectada");
        }

        if (connected && dbConn.isConnected()) {
            try {
                // Instância um BufferedReader, para receber os dados da serial
                input = serialPort.getInputStream();

                // Adiciona os Listeners na porta serial
                serialPort.addEventListener(this);
                serialPort.notifyOnDataAvailable(true);
            } catch (IOException | TooManyListenersException ex) {
                printLog("Erro da Porta Serial: " + ex.toString());
            }

            // Enquanto a porta serial estiver conectada e a base de dados também,
            // a Thread da leitura da serial continua viva
            while (serialPortIsConnected() && dbConn.isConnected()) {

            }
        }

        // Se passou do loop infinito fechar a conexão com o banco de dados e
        // com a porta serial
        // Tenta fechar a conexão com o banco de dados
        try {
            dbConn.closeConnection();
        } catch (ClassNotFoundException | SQLException | NullPointerException ex) {
            printLog("Erro de Banco de Dados: " + ex.toString());
        }

        // Fecha a porta serial
        closeSerialPort();

        // Thread morreu
        printLog("Terminou: " + this.getName());
    }

    // Método que cria um HashMap associando os identificadores das ports com
    // o nome
    public HashMap getCommPortIdentifiersByName() {

        //Cria o HashMap para guardar as ports
        HashMap portMap = new HashMap();

        //Cria o Enumeration que vai receber as ports
        Enumeration ports = null;

        //Pega os identificadores das ports
        ports = CommPortIdentifier.getPortIdentifiers();

        while (ports.hasMoreElements()) {

            CommPortIdentifier currentPort = (CommPortIdentifier) ports.nextElement();

            //seleciona apenas as ports que são seriais
            if (currentPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {

                //Mapeia a porta para poder trabalhar com ela a partir de seu nome
                portMap.put(currentPort.getName(), currentPort);
            }
        }
        return portMap;
    }

    // Verifica se a porta serial está conectado ao computador
    public boolean serialPortIsConnected() {

        // Pega as ports disponíveis
        HashMap availablePorts = getCommPortIdentifiersByName();

        // Caso o identificador da porta selecionada na Interface seja retornado
        // indica que a porta serial está conectada ao computador
        if (availablePorts.get(selectedPort) != null) {
            return true;
        } else {
            return false;
        }
    }

    // Fecha a porta serial
    public void closeSerialPort() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
            connected = false;
        }
    }

    // Método que implementa a leitura da porta serial
    public void serialEvent(SerialPortEvent oEvent) {

        // Se tiver dados na porta serial
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {

                Byte inputLine = (byte) input.read();
                dataReceived = new String(new byte[]{inputLine});

                if (dataReceived.equalsIgnoreCase("(")) {
                    pakageReceived = true;
                    answer = "";
                } else if (pakageReceived && !dataReceived.equalsIgnoreCase(")")) {
                    answer += dataReceived;
                } else if (pakageReceived && (answer.length() < 6 && dataReceived.equalsIgnoreCase(")"))) {
                    if (answer.equalsIgnoreCase("0,ED")) {
                        printLog("Erro de leitura do arduino");
                    } else if (answer.equalsIgnoreCase("ED,0")) {
                        printLog("Erro de leitura do sensor");
                    } else {
                        String[] data = answer.split(",");

                        if (data.length == 2) {

                            // O primeiro valor é a temperatura
                            temperature = data[0];
                            // O segundo valor é a umidade
                            humidity = data[1];
                            // Cria uma nova entidade médida
                            Measure measure = new Measure(Integer.parseInt(temperature), Integer.parseInt(humidity));
                            // Chama o método que executa o query do banco de dados
                            try {
                                dbConn.openConnection();
                            } catch (ClassNotFoundException ex) {
                                printLog("Erro ao reabrir conexão com o Banco de Dados");
                            }
                            meDAO.insert(measure, dbConn.getConnection());
                            printLog("Dados salvos: (" + answer + ")");
                        } else {
                            printLog("Erro na leitura da porta serial: Quantidade de dados acima do esperado");
                        }
                    }
                    // Como o pacote foi completamente recebido, limpa o buffer
                    freeBuffer();
                } else {
                    printLog("Falha na recepção do pacote, esvaziando buffers");
                    freeBuffer();
                }
            } catch (IOException | NumberFormatException e) {
                printLog("Erro na leitura da porta serial: " + e.toString());
            } catch (SQLException ex) {
                printLog("Erro de Banco de Dados: " + ex.toString());
                dbConn.setStatus(false);
            }
        }
    }

    // Método para limpar as variáveis que recebem os dados da serial
    private void freeBuffer() {
        dataReceived = "";
        answer = "";
        temperature = "";
        humidity = "";
        pakageReceived = false;
    }

    public void close() {
        connected = false;

        if (dbConn.getConnection() != null) {
            try {
                dbConn.closeConnection();
            } catch (ClassNotFoundException | SQLException ex) {
                printLog("Erro ao desconectar!");
            }
        }
    }
}
