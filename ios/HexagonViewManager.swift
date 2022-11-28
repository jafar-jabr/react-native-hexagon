import UIKit
import Foundation


@objc(HexagonViewManager)
class HexagonViewManager: RCTViewManager {


  override func view() -> (HexagonView) {
    return HexagonView()
  }

  @objc override static func requiresMainQueueSetup() -> Bool {
    return false
  }
}

class HexagonView : UIView {

    let rootController = ViewController();
    @IBOutlet weak var imageView: UIImageView!
    var rect = CGRectMake(0, 0, 48, 48)

    override init(frame: CGRect) {
           super.init(frame: frame)
            rect=frame
           self.addSubview(rootController.view);
       }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

  @objc var src: String = "" {
    didSet {
        print( "============= src = \(src)")

        rootController.setData(url: src)
    }
  }

    @objc var borderColor: String = "" {
      didSet {
          print( "============= borderColor = \(borderColor)")

          rootController.setBorderColor(_borderColor: borderColor)
      }
    }

    @objc var borderWidth: Int = 0 {
      didSet {
          print( "============= borderWidth = \(borderWidth)")

          rootController.setBorderWidth(_borderWidth: borderWidth)
      }
    }

    @objc var cornerRadius: Int = 0 {
      didSet {
          print( "============= cornerRadius = \(cornerRadius)")

          rootController.setCornerRadius(_cornerRadius: cornerRadius)
      }
    }

    @objc var width: Int = 0 {

         didSet {
             var mWidth=width+8
             rootController.setSize(_size:mWidth)
         }
       }
       @objc var height: Int = 0 {
         didSet {
             var mHeight=height+8
             rootController.setSize(_size:mHeight)

         }
       }


}
class ViewController: UIViewController {

    var imageView:UIImageView = UIImageView.init()
    var borderColor:String = ""
    var borderWidth:Int = 0
    var size:Int = 0
    var cornerRadius:Int = 0
    override func viewDidLoad() {
    super.viewDidLoad()
        addImageView()
     }

    func addImageView(name:String = "default"){

        if(size > 0 && borderColor != "" && borderWidth > 0 && cornerRadius > 0){
            let image = UIImage(named: name)
            imageView.image = image
            imageView.frame = CGRect(x: 0, y: 0, width: size, height: size)
            view.addSubview(imageView)
            let goodWidth = CGFloat(borderWidth) * 0.8
            setupHexagonImageView(imageView: imageView,cornerRadius:cornerRadius,lineWidth:Int(goodWidth.rounded(.down)),borderColor:borderColor)
        }

        }


    func setData( url :String){
        imageView.load(url: URL(string: url)!)

    }

    func setBorderColor( _borderColor :String) {
        print( "============= setBorderColor = \(_borderColor)")

       borderColor=_borderColor
        addImageView()

      }
    func setSize( _size :Int) {
        print( "============= setSize = \(_size)")
        //imageView.frame = CGRect(x: 0, y: 0, width: _size, height: _size)
        size=_size
        addImageView()
      }
    func  setBorderWidth( _borderWidth :Int) {
        print(" ============= setBorderWidth \(_borderWidth)")
        borderWidth=_borderWidth
        addImageView()
      }

    func setCornerRadius( _cornerRadius :Int) {
        cornerRadius=_cornerRadius
        addImageView()
        print( "============= setBorderWidth = \(_cornerRadius)")

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

    func setupHexagonImageView(imageView: UIImageView,cornerRadius:Int,lineWidth:Int,borderColor:String) {
        let _lineWidth: CGFloat = CGFloat(lineWidth)
        let path = roundedPolygonPath(rect: imageView.bounds, lineWidth: _lineWidth, sides: 6, cornerRadius: CGFloat(cornerRadius), rotationOffset: CGFloat(M_PI / 2.0))

       let mask = CAShapeLayer()
        mask.path = path.cgPath
       mask.lineWidth = _lineWidth
       mask.strokeColor = UIColor.clear.cgColor
       mask.fillColor = hexStringToUIColor(hexColor: borderColor).cgColor
       imageView.layer.mask = mask

       let border = CAShapeLayer()
        border.path = path.cgPath
       border.lineWidth = _lineWidth
       border.strokeColor = hexStringToUIColor(hexColor: borderColor).cgColor
       border.fillColor = UIColor.clear.cgColor
       imageView.layer.addSublayer(border)
   }

    func  roundedPolygonPath(rect: CGRect, lineWidth: CGFloat, sides: NSInteger, cornerRadius: CGFloat, rotationOffset: CGFloat = 0)
     -> UIBezierPath {
        let path = UIBezierPath()
        let theta: CGFloat = CGFloat(2.0 * M_PI) / CGFloat(sides) // How much to turn at every corner
        let offset: CGFloat = cornerRadius * 0     // Offset from which to start rounding corners
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
        viewController.willMove(toParent: self)
        viewController.view.frame = view.bounds
        view.addSubview(viewController.view)
        self.addChild(viewController)
        viewController.didMove(toParent: self)
    }
}

