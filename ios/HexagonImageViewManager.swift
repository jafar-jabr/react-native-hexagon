import UIKit
import Foundation
import React


@objc(HexagonImageViewManager)
class HexagonImageViewManager: RCTViewManager {
    
 func methodQueue() -> DispatchQueue {
            return bridge.uiManager.methodQueue
      }

  override func view() -> (HexagonImageView) {
    return HexagonImageView()
  }

  override class func requiresMainQueueSetup() -> Bool {
           return true
       }
}

class HexagonImageView : UIView {
    var rootController = ViewController();

    override open func layoutIfNeeded() {
        NSLog("[layoutIfNeeded] layoutIfNeeded")

    }
    
    @IBOutlet weak var imageView: UIImageView!
    var rect = CGRectMake(0, 0, 48, 48)
    var _source = NSDictionary()

    override public init(frame: CGRect) {
        super.init(frame: frame)
        rect=frame
        addSubview(rootController.view);
      }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc var source: NSDictionary = [:]  {

         didSet {
             _source=source
             rootController.setData(data: source,rect: rect)
         }
       }

    override open func layoutSubviews() {
        rect =
        CGRect(x:0,y:0,width:bounds.size.width,height:bounds.size.height)
        rootController.setData(data: _source,rect: rect)
      }
}

class ViewController: UIViewController {

    var imageView:UIImageView = UIImageView.init()
    var borderColor:String = ""
    var borderWidth:CGFloat = 0
    var cornerRadius:CGFloat = 0
    var viewRect = CGRectMake(0, 0, 48, 48)
    
    override func viewDidLoad() {
    super.viewDidLoad()
        addImageView()
     }


    func addImageView(name:String = "default"){
       
        if(borderColor != "" && borderWidth >= 0 && cornerRadius >= 0){
            imageView.frame = viewRect
            self.view.frame = viewRect
            self.view.layoutIfNeeded()
            view.addSubview(imageView)
            setupHexagonImageView(imageView: imageView,cornerRadius:cornerRadius,lineWidth:borderWidth,borderColor:borderColor)
        }

   }


    func setData( data :NSDictionary,rect : CGRect){
        viewRect=rect
        var dic:NSDictionary = NSDictionary(dictionary:data)

        guard let url = dic.object(forKey: "uri") as? String else {
            return
        }
        guard let _borderColor = dic.object(forKey: "borderColor") as? String else {
            return
        }
        guard let _borderWidth = dic.object(forKey: "borderWidth") as? CGFloat else {
            return
        }
        guard let _cornerRadius = dic.object(forKey: "cornerRadius") as? CGFloat else {
            return
        }

           borderColor = _borderColor
           borderWidth = _borderWidth * 0.7
           cornerRadius = _cornerRadius * 0.4
           imageView.load(url: URL(string: url  )!)
           addImageView()
       }

    func hexStringToUIColor(hexColor: String) -> UIColor {
      let stringScanner = Scanner(string: hexColor)

      if(hexColor.hasPrefix("#")) {
        stringScanner.scanLocation = 1
      }
      var color: UInt32 = 0
      stringScanner.scanHexInt32(&color)

      let r = CGFloat(Int(color >> 16) & 0x000000FF)
      let g = CGFloat(Int(color >> 8) & 0x000000FF)
      let b = CGFloat(Int(color) & 0x000000FF)

      return UIColor(red: r / 255.0, green: g / 255.0, blue: b / 255.0, alpha: 1)
    }

    func setupHexagonImageView(imageView: UIImageView,cornerRadius:CGFloat,lineWidth:CGFloat,borderColor:String) {

        let path = roundedPolygonPath(rect: imageView.bounds, lineWidth: lineWidth , sides: 6, cornerRadius: cornerRadius , rotationOffset: CGFloat(M_PI / 2.0))

       let mask = CAShapeLayer()
        mask.path = path.cgPath
       mask.lineWidth = lineWidth
       mask.strokeColor = UIColor.clear.cgColor
       mask.fillColor = hexStringToUIColor(hexColor: borderColor).cgColor
       imageView.layer.mask = mask

       let border = CAShapeLayer()
        border.path = path.cgPath
       border.lineWidth = lineWidth
       border.strokeColor = hexStringToUIColor(hexColor: borderColor).cgColor
       border.fillColor = UIColor.clear.cgColor
       imageView.layer.addSublayer(border)
   }

    func  roundedPolygonPath(rect: CGRect, lineWidth: CGFloat, sides: NSInteger, cornerRadius: CGFloat, rotationOffset: CGFloat = 0)
     -> UIBezierPath {
        let path = UIBezierPath()
        let theta: CGFloat = CGFloat(2.0 * M_PI) / CGFloat(sides) // How much to turn at every corner
        let offset: CGFloat = cornerRadius * tan(theta / 2.0)     // Offset from which to start rounding corners
        let width = min(rect.size.width, rect.size.height)        // Width of the square

        let center = CGPoint(x: rect.origin.x + width / 2.0, y: rect.origin.y + width / 2.0)

        // Radius of the circle that encircles the polygon
        // Notice that the radius is adjusted for the corners, that way the largest outer
        // dimension of the resulting shape is always exactly the width - linewidth
        let radius = (width - lineWidth + cornerRadius - (cos(theta) * cornerRadius)) / 2.0

        // Start drawing at a point, which by default is at the right hand edge
        // but can be offset
        var angle = CGFloat(rotationOffset)

        let corner = CGPointMake(center.x + (radius - cornerRadius) * cos(angle), center.y + (radius - cornerRadius) * sin(angle))
         path.move(to: CGPointMake(corner.x + cornerRadius * cos(angle + theta), corner.y + cornerRadius * sin(angle + theta)))

        for _ in 0 ..< sides {
            angle += theta

            let corner = CGPointMake(center.x + (radius - cornerRadius) * cos(angle), center.y + (radius - cornerRadius) * sin(angle))
            let tip = CGPointMake(center.x + radius * cos(angle), center.y + radius * sin(angle))
            let start = CGPointMake(corner.x + cornerRadius * cos(angle - theta), corner.y + cornerRadius * sin(angle - theta))
            let end = CGPointMake(corner.x + cornerRadius * cos(angle + theta), corner.y + cornerRadius * sin(angle + theta))

            path.addLine(to: start)
            path.addQuadCurve(to: end, controlPoint: tip)
        }

         path.close()

        // Move the path to the correct origins
        let bounds = path.bounds
       // let transform = CGAffineTransformMakeTranslation(-bounds.origin.x + rect.origin.x + lineWidth / 2.0,-bounds.origin.y + rect.origin.y + lineWidth / 2.0)
         let transform = CGAffineTransform(translationX: 0,
         y: 0)
         path.apply(transform)

        return path
    }
}


extension UIImageView {
    func load(url: URL) {
        DispatchQueue.global().async { [weak self] in
            if let data = try? Data(contentsOf: url) {
                if let image = UIImage(data: data) {
                    DispatchQueue.main.async {
                        self?.image = image
                    }
                }
            }
        }
    }
    
}
extension UIViewController {
    func embed(_ viewController:UIViewController, inView view:UIView){
        self.edgesForExtendedLayout = UIRectEdge.bottom;
        self.extendedLayoutIncludesOpaqueBars = true
        
        viewController.willMove(toParent: self)
        viewController.view.frame = view.bounds
        view.addSubview(viewController.view)
        self.addChild(viewController)
        viewController.didMove(toParent: self)
    }
}

