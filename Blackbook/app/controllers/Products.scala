package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import java.io.File
import scala.collection.mutable.ArrayBuffer

object Products extends Controller {

  val productForm = Form (
    tuple("name" -> nonEmptyText,
          "description" -> nonEmptyText)
  )
  
  def products = Action {
    Ok(views.html.products.index(Product.all(), productForm))
  }
  
  def viewProduct(id: Long) = Action {
    val product = Product.find(id)
    product match { 
      case Some(p) => Ok(views.html.products.view(p))
      case None => BadRequest(views.html.products.index(Product.all(), productForm))
    }
  }
  
  def editProduct(id: Long) = Action {
    val product = Product.find(id)
    product match { 
      case Some(p) => Ok(views.html.products.edit(p,productForm.fill((p.name, p.description))))
      case None => throw new Exception("No product " + id + " found.")
    }
  }
  
  def updateProduct(id: Long) = Action { implicit request =>
    productForm.bindFromRequest.fold(
      errors => BadRequest(views.html.products.index(Product.all(), errors)),
      form => {
        val (name, description) = form
        val product = Product.find(id)
	    product match { 
	      case Some(p) => {Product.update(p.id, name, description); Redirect(routes.Products.products)}
	      case None => BadRequest(views.html.products.index(Product.all(), productForm))
	    }
      })
  }

  def newProduct = Action { implicit request =>
    productForm.bindFromRequest.fold(
      errors => BadRequest(views.html.products.index(Product.all(), errors)),
      form => {
        val (name, description) = form
        Product.create(name, description)
        Redirect(routes.Products.products)
      })
  }

  def deleteProduct(id: Long) = Action {
    Product.delete(id)
    Redirect(routes.Products.products)
  }

  def tagsForProduct(id: Long) = Action { 
    val product = Product.find(id)

    product match { 
      case Some(p) => Ok(views.html.product_tags(p))
      case None => BadRequest(views.html.products.index(Product.all(), productForm))
    }
  }
  
  private[this] def getProductFilePath(id: Long, filename:String):File = {
    return new File("/tmp/products/" + id + "/files/" + filename)
  }
  
  def listFiles(id: Long): List[File] = {
    var files = ArrayBuffer[File]()
    for(file <- new File("/tmp/products/" + id + "/files/").listFiles()){
      files += file
    }
    return files.toList
  }
  
  def getFile(id: Long, filename:String) = Action {
    Ok.sendFile(getProductFilePath(id, filename))
  }
  
  def deleteFile(id: Long, filename:String) = Action {
    getProductFilePath(id, filename).delete()
    Redirect(routes.Products.editProduct(id))
  }
  
  def uploadProductFile(id: Long) = Action(parse.multipartFormData) { request =>
	  request.body.file("fileUpload").map { fileUpload =>
	    val filename = fileUpload.filename 
	    val contentType = fileUpload.contentType
	    fileUpload.ref.moveTo(getProductFilePath(id,filename), replace=true)
	    Redirect(routes.Products.editProduct(id))
	  }.getOrElse {
	    Redirect(routes.Products.editProduct(id)).flashing("error" -> "Missing file")
	  }
  }
  
}