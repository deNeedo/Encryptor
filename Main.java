import java.io.File; import java.io.FileReader;

public class Main {
    public static void main(String[] args)
    {
        try
        {
            File file = new File("./data");
            FileReader reader = new FileReader(file);
            int data;
            while ((data = reader.read()) != -1)
            {
                System.out.println(data);
                
            }
            reader.close();
        }
        catch (Exception e)
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
