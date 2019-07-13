import UIKit
import app

class ViewController: UIViewController, ViewUpdater {
    func dataUpdate(t: String) {
        label.text = t
    }
    
    @IBOutlet weak var button: UIButton!
    
    @IBAction func runStuff(_ sender: Any) {
        Collections().putSample(s: "Hello iOS")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        label.text = Proxy().proxyHello()
        StateKt.viewUpdater = self
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    @IBOutlet weak var label: UILabel!
}
