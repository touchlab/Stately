import UIKit
import app

class ViewController: UIViewController {
    let ktstate = State()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        label.text = Proxy().proxyHello()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    @IBOutlet weak var label: UILabel!
    @IBOutlet weak var textView: UITextField!
    @IBAction func addSample(_ sender: Any) {
        ktstate.putSample(s: textView.text!)
        
    }
    @IBAction func printValues(_ sender: Any) {
        ktstate.printAll()
    }
}
