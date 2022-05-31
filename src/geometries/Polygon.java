package geometries;

import java.util.List;

import primitives.*;
import static primitives.Util.*;

/**
 * Polygon class represents two-dimensional polygon in 3D Cartesian coordinate
 * system
 * 
 * @author Dan
 */
public class Polygon extends Geometry {
	/**
	 * List of polygon's vertices
	 */
	protected List<Point> vertices;
	/**
	 * Associated plane in which the polygon lays
	 */
	protected Plane plane;
	private int size;


	/**
	 * Polygon constructor based on vertices list. The list must be ordered by edge
	 * path. The polygon must be convex.
	 * 
	 * @param vertices list of vertices according to their order by edge path
	 * @throws IllegalArgumentException in any case of illegal combination of
	 *                                  vertices:
	 *                                  <ul>
	 *                                  <li>Less than 3 vertices</li>
	 *                                  <li>Consequent vertices are in the same
	 *                                  point
	 *                                  <li>The vertices are not in the same
	 *                                  plane</li>
	 *                                  <li>The order of vertices is not according
	 *                                  to edge path</li>
	 *                                  <li>Three consequent vertices lay in the
	 *                                  same line (180&#176; angle between two
	 *                                  consequent edges)
	 *                                  <li>The polygon is concave (not convex)</li>
	 *                                  </ul>
	 */
	public Polygon(Point... vertices) {
		if (vertices.length < 3)
			throw new IllegalArgumentException("A polygon can't have less than 3 vertices");
		this.vertices = List.of(vertices);
		// Generate the plane according to the first three vertices and associate the
		// polygon with this plane.
		// The plane holds the invariant normal (orthogonal unit) vector to the polygon
		plane = new Plane(vertices[0], vertices[1], vertices[2]);
		if (vertices.length == 3)
			return; // no need for more tests for a Triangle

		Vector n = plane.getNormal();

		// Subtracting any subsequent points will throw an IllegalArgumentException
		// because of Zero Vector if they are in the same point
		Vector edge1 = vertices[vertices.length - 1].subtract(vertices[vertices.length - 2]);
		Vector edge2 = vertices[0].subtract(vertices[vertices.length - 1]);

		// Cross Product of any subsequent edges will throw an IllegalArgumentException
		// because of Zero Vector if they connect three vertices that lay in the same
		// line.
		// Generate the direction of the polygon according to the angle between last and
		// first edge being less than 180 deg. It is hold by the sign of its dot product
		// with
		// the normal. If all the rest consequent edges will generate the same sign -
		// the
		// polygon is convex ("kamur" in Hebrew).
		boolean positive = edge1.crossProduct(edge2).dotProduct(n) > 0;
		for (var i = 1; i < vertices.length; ++i) {
			// Test that the point is in the same plane as calculated originally
			if (!isZero(vertices[i].subtract(vertices[0]).dotProduct(n)))
				throw new IllegalArgumentException("All vertices of a polygon must lay in the same plane");
			// Test the consequent edges have
			edge1 = edge2;
			edge2 = vertices[i].subtract(vertices[i - 1]);
			if (positive != (edge1.crossProduct(edge2).dotProduct(n) > 0))
				throw new IllegalArgumentException("All vertices must be ordered and the polygon must be convex");
		}
		size = vertices.length;
	}

	@Override
	public Vector getNormal(Point point) {
		return plane.getNormal();
	}

	@Override
	public List<GeoPoint> findGeoIntersectionsHelper(Ray ray) {
		if (box != null && !box.IsRayHitBox(ray))
			return null;
		//get Intersections of plane
		List<GeoPoint> planeIntersections = plane.findGeoIntersections(ray);

		if (planeIntersections == null)
			return null;

		Point p0 = ray.getP0();
		Vector rayDir = ray.getDir();

		//all the vectors ( (v1-p0)x(v2-p0) ) * (ray dir)  should be the same signe
		// else the ray outside the polygon

		//first check the sign of dot product the last and the first
		Vector v1 = vertices.get(0).subtract(p0);
		Vector vn = vertices.get(vertices.size() - 1).subtract(p0);

		double s1 = rayDir.dotProduct(vn.crossProduct(v1));

		//if the ray cross in the edge of the polygon
		if (isZero(s1))
			return null;

		double s2; //keep the next product
		Vector v2; //the next vector
		for (var vertex : vertices.subList(1, vertices.size())) {

			v2 = vertex.subtract(p0);
			s2 = rayDir.dotProduct(v1.crossProduct(v2));

			//if the ray cross in the edge of the polygon
			if (isZero(s2)) return null;

			//if they not the same sign
			if( s1 * s2 < 0  )
				return null;

			v1 = v2; //for the next round
		}

		//if the func not return null than we have Intersections with the polygon
		return planeIntersections.stream().map(gp->new GeoPoint(this,gp.point)).toList();
	}
	/**
	 * Set box for polygon
	 */
	@Override
	public void setBox() {
		Point pointI;
		Point point = vertices.get(0);

		//initialize max and min in the first element of vertices list
		double maxX = point.getX();
		double maxY = point.getY();
		double maxZ = point.getZ();
		double minX = maxX;
		double minY = maxY;
		double minZ = maxZ;
		for (int i = 1; i < vertices.size(); i++) {	//loop for that find the max and min in all of the
			pointI = vertices.get(i);										//coordinate of the polygon
			if (maxX < pointI.getX())
				maxX = pointI.getX();

			if (maxY < pointI.getY())
				maxY = pointI.getY();

			if (maxZ < pointI.getZ())
				maxZ = pointI.getZ();

			if (minX > pointI.getX())
				minX = pointI.getX();

			if (minY > pointI.getY())
				minY = pointI.getY();

			if (minZ > pointI.getZ())
				minZ = pointI.getZ();
		}
		box = new Box(maxX, maxY, maxZ, minX, minY, minZ);
	}
}
